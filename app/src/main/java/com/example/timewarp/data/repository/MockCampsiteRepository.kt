package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.repository.CampsiteRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * NEEDS_DOCUMENTATION
 */
class MockCampsiteRepository @Inject constructor(): CampsiteRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsites(): MutableList<Campsite>? {
        return mutableListOf(
            Campsite(
                campsiteId = "1",
                ownerUserId = "owner_1",
                name = "Townsite Campground",
                description = "",
                rating = 4.6,
                location = Location(address = "Townsite Road, Jasper, Alberta"),
                occupancy = Occupancy(maxGuests = 6),
                pricing = Pricing(costPerNight = 45.0)
            ),
            Campsite(
                campsiteId = "2",
                ownerUserId = "owner_2",
                name = "Two Jack Main",
                description = "",
                rating = 4.6,
                location = Location(address = "Two Jack Lake Road, Banff, Alberta"),
                occupancy = Occupancy(maxGuests = 6),
                pricing = Pricing(costPerNight = 45.0)
            )
        )
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
    override fun observeCampsite(campsiteId: String): Flow<Campsite?> = flowOf(
        TODO("Not yet implemented")
    )

    /**
     * Returns the mock campsites owned by [ownerUserId].
     */
    override fun observeOwnerCampsites(ownerUserId: String): Flow<List<Campsite>> = flow {
        val owned = mutableListOf<Campsite>()
        for (campsite in getCampsites().orEmpty()) {
            if (campsite.ownerUserId == ownerUserId) {
                owned.add(campsite)
            }
        }
        emit(owned)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun filterCampsites(filter: CampsiteFilter): MutableList<Campsite>? =
        getCampsites().orEmpty().filter { filter.matches(it) }.toMutableList()
}