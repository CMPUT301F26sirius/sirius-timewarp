package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [CampsiteRepository].
 *
 * Stores and retrieves campsites from a `campsites` collection.
 */
@Singleton
class FakeCampsiteRepository @Inject constructor() : CampsiteRepository {
    private val campsites = mutableListOf<Campsite>()

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsites(): MutableList<Campsite>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createCampsite(campsite: Campsite) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editCampsite(campsiteId: String, campsite: Campsite) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsite(campsiteId: String): Campsite? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun queryCampsites(query: String): MutableList<Campsite> {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsite(campsiteId: String): Flow<Campsite?> = callbackFlow {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun filterCampsites(location: String, guests: Int?): MutableList<Campsite>? {
        return campsites
            .filter { campsite ->
                val locationMatches = location.isBlank() ||
                    campsite.location.address.contains(location, ignoreCase = true)
                val guestsMatch = guests == null || campsite.occupancy.maxGuests >= guests
                locationMatches && guestsMatch
            }
            .toMutableList()
    }
}