package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ReservationRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Transaction
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ReservationRepository].
 *
 * Stores and retrieves reservations from the Firestore `reservation` collection.
 *
 * @property firestore Firestore instance used to access reservations.
 */
@Singleton
class FirebaseReservationRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ReservationRepository {
    /**
     * Gets a reservation by reservationId
     *
     * @param reservationId
     * @returns a [Reservation]
     */
    override suspend fun getReservation(reservationId: String): Reservation? {
        val snapshot = firestore.collection("reservation")
            .document(reservationId)
            .get()
            .await()

        // No document with this id
        if (!snapshot.exists()) {
            return null
        }

        val reservation = snapshot.toObject(Reservation::class.java)
        if (reservation == null) {
            return null
        }

        // The id is the document name, not a field inside it
        return reservation.copy(reservationId = reservationId)
    }

    /**
     * Updates a reservations state to a ReservationState in a transaction to ensure
     * that the active winner count in the lottery is updated correctly as well.
     *
     * @param reservationId
     * @param state
     */
    override suspend fun updateReservationState(reservationId: String, state: ReservationState) {
        val reservationRef = firestore.collection("reservation").document(reservationId)
        val existing = getReservation(reservationId)
        existing?.let { ensureWinnerCountsInitialized(mapOf(it to it.copy(reservationState = state))) }

        // Begin a transaction
        firestore.runTransaction { transaction ->
            val reservationSnapshot = transaction.get(reservationRef)
            val reservation = reservationSnapshot.toReservation(reservationId) ?: throw IllegalStateException("Reservation $reservationId does not exist")
            val updated = reservation.copy(reservationState = state)

            // Pass transaction for the winner count
            adjustWinnerCounts(transaction, mapOf(reservation to updated))
            transaction.update(reservationRef, "reservationState", state.name)
        }.await()
    }

    /**
     * Gets all reservations
     *
     * @return a list of [Reservation]
     */
    override suspend fun getReservations(): MutableList<Reservation>? {
        val snapshot = firestore.collection("reservation")
            .get()
            .await()
        val reservations = mutableListOf<Reservation>()
        for (document in snapshot.documents) {
            val reservation = document.toObject(Reservation::class.java) ?: continue
            reservations.add(reservation.copy(reservationId = document.id))
        }
        return reservations
    }

    /**
     * Gets all waitlisted reservations at a give lottery
     *
     * @param lotteryId, identifier of the lottery
     * @return a list of [Reservation]
     */
    override suspend fun getWaitlistedReservations(lotteryId: String): List<Reservation> {
        val snapshot = firestore.collection("reservation")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .await()

        return snapshot.documents.mapNotNull { document ->
            document.toReservation(document.id)
                ?.takeIf { it.reservationState == ReservationState.Waitlisted }
        }
    }

    /**
     * Gets the active winner count for a lottery, or initializes it if unset
     *
     * @param lotteryId
     * @return active winners [Int]
     */
    override suspend fun getOrInitializeActiveWinnerCount(lotteryId: String): Int {
        val lotteryReference = firestore.collection("lottery").document(lotteryId)
        val lotterySnapshot = lotteryReference.get().await()
        if (!lotterySnapshot.exists()) {
            throw IllegalStateException("Lottery $lotteryId does not exist")
        }
        lotterySnapshot.getLong("activeWinnerCount")?.toInt()?.let { return it }
        val countedWinners = countActiveWinners(lotteryId)

        return firestore.runTransaction { transaction ->
            val currentSnapshot = transaction.get(lotteryReference)
            if (!currentSnapshot.exists()) {
                throw IllegalStateException("Lottery $lotteryId does not exist")
            }
            currentSnapshot.getLong("activeWinnerCount")?.toInt()
                ?: countedWinners.also { count ->
                    transaction.update(lotteryReference, "activeWinnerCount", count)
                }
        }.await()
    }

    /**
     * Attempts to promote the specified reservation for the given lottery to Pending from
     * Waitlisted by performing a transaction.
     *
     * @param reservationId
     * @return active winners [Int]
     */
    override suspend fun tryPromoteWaitlistedReservation(
        reservationId: String,
        promoteNotification: Notification,
    ): Boolean {
        // Get references
        val reservationReference = firestore.collection("reservation").document(reservationId)
        val notificationReference = firestore.collection("notification").document(promoteNotification.notificationId)

        // A missing counter is initialized outside the transaction because Firestore's
        // Android transaction API supports document reads, not query reads.
        val promotionResult = firestore.runTransaction<Boolean?> { transaction ->
            // Get the reservation
            val reservationSnapshot = transaction.get(reservationReference)
            val reservation = reservationSnapshot.toReservation(reservationId) ?: return@runTransaction false
            val lotteryId = reservation.lotteryId
            if (lotteryId.isBlank() || reservation.reservationState != ReservationState.Waitlisted) {
                return@runTransaction false
            }

            // Get the lottery
            val lotteryReference = firestore.collection("lottery").document(lotteryId)
            val lotterySnapshot = transaction.get(lotteryReference)
            if (!lotterySnapshot.exists()) { throw IllegalStateException("Lottery $lotteryId does not exist") }
            val lottery = lotterySnapshot.toObject(Lottery::class.java) ?: throw IllegalStateException("Lottery $lotteryId could not be read")

            // Get the winner count, and activeWinnerCount
            val winnerCount = lottery.winnerCount
            val activeWinnerCount = lotterySnapshot.getLong("activeWinnerCount")
                ?: return@runTransaction null

            // Return if enough winners have been drawn already
            if (activeWinnerCount >= winnerCount) {
                return@runTransaction false
            }

            // Commit
            transaction.update(reservationReference, "reservationState", ReservationState.Pending.name)
            transaction.update(lotteryReference, "activeWinnerCount", activeWinnerCount + 1)
            transaction.set(notificationReference, promoteNotification)
            true
        }.await()
        if (promotionResult != null) return promotionResult

        val reservation = getReservation(reservationId) ?: return false
        if (reservation.lotteryId.isBlank() ||
            reservation.reservationState != ReservationState.Waitlisted
        ) {
            return false
        }
        getOrInitializeActiveWinnerCount(reservation.lotteryId)
        return tryPromoteWaitlistedReservation(reservationId, promoteNotification)
    }

    /**
     * Creates a reservation & adjusts winner counts
     *
     * @param reservation
     */
    override suspend fun createReservation(reservation: Reservation) {
        val reservationRef = firestore.collection("reservation").document(reservation.reservationId)
        val existing = getReservation(reservation.reservationId)
        ensureWinnerCountsInitialized(mapOf(existing to reservation))

        // Begin a transaction
        firestore.runTransaction { transaction ->
            val existingSnapshot = transaction.get(reservationRef)
            val existing = existingSnapshot.toReservation(reservation.reservationId)

            // Pass transaction for the winner count
            adjustWinnerCounts(transaction, mapOf(existing to reservation))
            transaction.set(reservationRef, reservation)
        }.await()
    }

    /**
     * Replaces the stored reservation with [reservationId] with new data and adjusts winner counts.
     *
     * @param reservationId the reservation to overwrite.
     * @param campsite the new reservation data (named campsite in the interface).
     */
    override suspend fun editReservation(reservationId: String, campsite: Reservation) {
        val reservationRef = firestore.collection("reservation").document(reservationId)
        val updated = campsite.copy(reservationId = reservationId)
        val existing = getReservation(reservationId)
        ensureWinnerCountsInitialized(mapOf(existing to updated))

        // Begin a transaction
        firestore.runTransaction { transaction ->
            val existingSnapshot = transaction.get(reservationRef)
            val existing = existingSnapshot.toReservation(reservationId)

            // Pass transaction for the winner count
            adjustWinnerCounts(transaction, mapOf(existing to updated))
            transaction.set(reservationRef, updated)
        }.await()
    }

    /**
     * @param reservationId the reservation to overwrite.
     */
    override suspend fun cancelReservation(reservationId: String) {
        updateReservationState(reservationId, ReservationState.Canceled)
    }

    /**
     * Deletes a reservation from Firestore & adjusts winner counts
     *
     * @param reservationId identifier of the reservation
     */
    override suspend fun deleteReservation(reservationId: String) {
        val reservationRef = firestore.collection("reservation").document(reservationId)
        val existing = getReservation(reservationId)
        existing?.let { ensureWinnerCountsInitialized(mapOf(it to null)) }

        // Begin a transaction
        firestore.runTransaction { transaction ->
            val reservationSnapshot = transaction.get(reservationRef)
            val existing = reservationSnapshot.toReservation(reservationId)

            // Pass transaction for the winner count
            adjustWinnerCounts(transaction, mapOf(existing to null))
            transaction.delete(reservationRef)
        }.await()
    }

    /**
     * Listens to the Firestore spot for reservation and emits the full list so that we
     * can have real time updates
     *
     * @return a flow of list of reservations
     */
    override fun observeReservations(): Flow<List<Reservation>> = callbackFlow {
        val listener = firestore.collection("reservation")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val reservations = mutableListOf<Reservation>()
                for (document in snapshot?.documents.orEmpty()) {
                    val reservation = document.toObject(Reservation::class.java) ?: continue
                    reservations.add(reservation.copy(reservationId = document.id))
                }
                trySend(reservations)
            }

        awaitClose {
            listener.remove()
        }
    }

    /**
     * Changes activeWinnerCount for a set of changes to reservations, and their states
     *
     * @param changes map
     */
    private suspend fun ensureWinnerCountsInitialized(changes: Map<Reservation?, Reservation?>) {
        val deltas = mutableMapOf<String, Int>()
        changes.forEach { (old, new) ->
            if (old != null && old.isActiveWinner()) { deltas[old.lotteryId] = (deltas[old.lotteryId] ?: 0) - 1 }
            if (new != null && new.isActiveWinner()) { deltas[new.lotteryId] = (deltas[new.lotteryId] ?: 0) + 1 }
        }
        deltas.filter { (lotteryId, delta) ->
            lotteryId.isNotBlank() && delta != 0
        }.keys.forEach { lotteryId ->
            getOrInitializeActiveWinnerCount(lotteryId)
        }
    }

    /**
     * Changes activeWinnerCount for a set of changes to reservations, and their states.
     * Transaction based method.
     *
     * @param transaction
     * @param changes map
     */
    private fun adjustWinnerCounts(transaction: Transaction, changes: Map<Reservation?, Reservation?>) {
        // Changes for each lottery
        val deltas = mutableMapOf<String, Int>()
        changes.forEach { (old, new) ->
            if (old != null && old.isActiveWinner()) { deltas[old.lotteryId] = (deltas[old.lotteryId] ?: 0) - 1 }
            if (new != null && new.isActiveWinner()) { deltas[new.lotteryId] = (deltas[new.lotteryId] ?: 0) + 1 }
        }

        // Count for each, pairs of lotteryIds and new count
        val counts = deltas.filter { (lotteryId, delta) ->
            lotteryId.isNotBlank() && delta != 0
        }.mapNotNull { (lotteryId, delta) ->
            readActiveWinnerCount(transaction, lotteryId)?.let { lotteryId to (it + delta) }
        }

        // For each updated lottery, set new count
        counts.forEach { (lotteryId, updatedCount) ->
            check(updatedCount >= 0) {
                "Active winner count for lottery $lotteryId would become negative"
            }
            transaction.update(
                firestore.collection("lottery").document(lotteryId),
                "activeWinnerCount",
                updatedCount,
            )
        }
    }

    /**
     * Read active winner count through a transaction
     *
     * @param transaction
     * @param lotteryId
     */
    private fun readActiveWinnerCount(transaction: Transaction, lotteryId: String): Int? {
        val lotterySnapshot = transaction.get(firestore.collection("lottery").document(lotteryId))
        if (!lotterySnapshot.exists()) { return null }
        return lotterySnapshot.getLong("activeWinnerCount")?.toInt()
            ?: throw IllegalStateException("Active winner count for lottery $lotteryId was not initialized")
    }

    /**
     * Count active winners by querying reservations for one lottery.
     *
     * @param lotteryId
     */
    private suspend fun countActiveWinners(lotteryId: String): Int {
        val snapshot = firestore.collection("reservation")
            .whereEqualTo("lotteryId", lotteryId)
            .get()
            .await()
        return snapshot.documents.count { document ->
            document.toObject(Reservation::class.java).isActiveWinner()
        }
    }

    /**
     * Helper that converts document snapshots into reservations, since it was getting a bit
     * repetitive.
     *
     * @param reservationId - The reservationId of the snapshot
     * @returns A reservation copy
     */
    private fun DocumentSnapshot.toReservation(reservationId: String): Reservation? {
        return toObject(Reservation::class.java)?.copy(reservationId = reservationId)
    }

    /**
     * Helper that checks if a reservation is an active winner,
     * i.e. either a Pending or Reserved reservation
     */
    private fun Reservation?.isActiveWinner(): Boolean {
        return this?.reservationState == ReservationState.Pending || this?.reservationState == ReservationState.Reserved
    }
}

