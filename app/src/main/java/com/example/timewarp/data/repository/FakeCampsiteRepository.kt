package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCampsiteRepository: CampsiteRepository {
    override fun getCampsites(): Flow<List<Campsite>> {
        return flowOf(
            listOf(

                Campsite(
                    id = "1",
                    name = "Beer Creek Campground",
                    location = "Drayton Valley, Alberta",
                    maximumOccupancy = 5,
                    rating = 5.0
                ),

                Campsite(
                    id = "2",
                    name = "Crimson Lake Provincial Park",
                    location = "Rocky Mountain House, Alberta",
                    maximumOccupancy = 8,
                    rating = 4.6
                ),

                Campsite(
                    id = "3",
                    name = "Crimson Lake Campground",
                    location = "Clearwater County, Alberta",
                    maximumOccupancy = 15,
                    rating = 4.6
                )
            )
        )
    }
}