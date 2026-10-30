package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
interface CampsiteRepository {
    suspend fun createCampsite(campsite: Campsite)

    suspend fun editCampsite(campsiteId: String, campsite: Campsite)

    suspend fun getCampsite(campsiteId: String): Campsite?

    suspend fun queryCampsites(query: String): MutableList<Campsite>
}