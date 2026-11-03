package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.repository.CampsiteRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
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
                location = Location(address = ""),
                occupancy = Occupancy(maxGuests = 6),
                pricing = Pricing(costPerNight = 45.0)
            ),
            Campsite(
                campsiteId = "2",
                ownerUserId = "owner_2",
                name = "Two Jack Main",
                description = "",
                rating = 4.6,
                location = Location(address = ""),
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
        null
    )

    override suspend fun filterCampsites(location: String, guests: Int?): MutableList<Campsite>? {
        return getCampsites().orEmpty().filter {
            campsite -> (guests == null || campsite.occupancy.maxGuests >= guests) &&
                (location.isBlank() || campsite.location.address.contains(location.trim(), ignoreCase = true))
        }.toMutableList()
    }
}