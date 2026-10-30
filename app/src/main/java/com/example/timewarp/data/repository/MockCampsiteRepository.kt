package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import jakarta.inject.Inject

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
}