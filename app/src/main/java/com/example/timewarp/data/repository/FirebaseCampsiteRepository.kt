package com.example.timewarp.data.repository

import androidx.compose.runtime.mutableStateOf
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.CampsiteRepository
import com.google.firebase.firestore.FirebaseFirestore
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
}