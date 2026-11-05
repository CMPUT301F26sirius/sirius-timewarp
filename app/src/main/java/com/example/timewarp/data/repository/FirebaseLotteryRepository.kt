package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [LotteryRepository].
 *
 * Stores and retrieves lotteries from the Firestore `lottery` collection.
 *
 * @property firestore Firestore instance used to access user documents.
 */
@Singleton
class FirebaseLotteryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LotteryRepository {
    /**
     * Write a new lottery entry to Firestore 'lottery' collection
     *
     * document generated ID for lotteryID
     *
     * @param lottery Instance of lottery to write to Firestore
     */
    override suspend fun createLottery(lottery: Lottery) {
        val document = firestore.collection("lottery").document()
        document.set(lottery.copy(lotteryId = document.id)).await()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editLottery(lotteryId: String, lottery: Lottery) {
        TODO("Not yet implemented")

    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLotteries(): MutableList<Lottery>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLottery(lotteryId: String): Lottery? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>> = callbackFlow {
        val listener = firestore.collection("lottery")
            .whereArrayContains("campsiteIds", campsiteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val lotteries = snapshot
                    ?.documents
                    .orEmpty()
                    .mapNotNull { it.toObject(Lottery::class.java)?.copy(lotteryId = it.id) }

                trySend(lotteries)
            }

        awaitClose {
            listener.remove()
        }
    }
}