package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [CampsiteRepository].
 *
 * Stores and retrieves campsites from the Firestore `campsites` collection.
 *
 * @property firestore Firestore instance used to access user documents.
 */
@Singleton
class FirebaseCampsiteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : CampsiteRepository {
    /**
     * Gets all campsites from Firestore.
     *
     * @returns A list of campsite objects.
     */
    override suspend fun getCampsites(): MutableList<Campsite>? {
        TODO("Not yet implemented")
    }

    /**
     * Writes to a campsites Firestore document.
     *
     * @param campsite Campsite object to write.
     */
    override suspend fun createCampsite(campsite: Campsite) {
        TODO("Not yet implemented")
    }

    /**
     * Edits a campsites Firestore document.
     *
     * @param campsiteId id of the campsite document to edit.
     * @param campsite Campsite object to write.
     */
    override suspend fun editCampsite(campsiteId: String, campsite: Campsite) {
        TODO("Not yet implemented")
    }

    /**
     * Retrieves a campsite by its id.
     *
     * @param campsiteId id of the campsite document to retrieve.
     * @return the campsite if the document exists, and `null` otherwise.
     */
    override suspend fun getCampsite(campsiteId: String): Campsite? {
        TODO("Not yet implemented")
    }

    /**
     * Query a list of campsites. Might be a better way to do this.
     * Need more filters for the query (date, occupancy, etc)
     *
     * @param query string query
     */
    override suspend fun queryCampsites(query: String): MutableList<Campsite> {
        TODO("Not yet implemented")
    }

    override fun observeCampsite(campsiteId: String): Flow<Campsite?> = callbackFlow {
        val listener = firestore.collection("campsite")
            .document(campsiteId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val campsite = snapshot
                    ?.takeIf { it.exists() }
                    ?.toObject(Campsite::class.java)
                    ?.copy(campsiteId = campsiteId)

                trySend(campsite)
            }

        awaitClose {
            listener.remove()
        }
    }
}