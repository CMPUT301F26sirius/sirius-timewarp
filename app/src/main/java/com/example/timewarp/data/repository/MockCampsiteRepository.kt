package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.repository.CampsiteRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MockCampsiteRepository @Inject constructor(): CampsiteRepository {
//    override fun getCampsites(): Flow<List<Campsite>> {
//        return flowOf(
//            listOf(
//
//                Campsite(
//                    id = "1",
//                    ownerId = "",
//                    name = "Beer Creek Campground",
//                    description = "....",
//                    location = Location("Drayton Valley, Alberta"),
//                    occupancy = Occupancy(maxGuests = 8),
//                    pricing = Pricing(),
//                    rating = 4.7
//                ),
//
//                Campsite(
//                    id = "2",
//                    ownerId = "",
//                    name = "Crimson Lake Provincial Park",
//                    description = "....",
//                    location = Location("Rocky Mountain House, Alberta"),
//                    occupancy = Occupancy(maxGuests = 15),
//                    pricing = Pricing(),
//                    rating = 3.0
//                ),
//
//                Campsite(
//                    id = "3",
//                    ownerId = "",
//                    name = "Crimson Lake Campground",
//                    description = "....",
//                    location = Location("Clearwater County, Alberta"),
//                    occupancy = Occupancy(maxGuests = 20),
//                    pricing = Pricing(),
//                    rating = 4.9
//                )
//            )
//        )
//    }

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


    override suspend fun createCampsite(campsite: Campsite) {
        TODO("Not yet implemented")
    }

    override suspend fun editCampsite(campsiteId: String, campsite: Campsite) {
        TODO("Not yet implemented")
    }

    override suspend fun getCampsite(campsiteId: String): Campsite? {
        TODO("Not yet implemented")
    }

    override suspend fun queryCampsites(query: String): MutableList<Campsite> {
        TODO("Not yet implemented")
    }

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