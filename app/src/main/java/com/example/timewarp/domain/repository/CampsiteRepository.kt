package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import kotlinx.coroutines.flow.Flow

interface CampsiteRepository {
    suspend fun getCampsites(): MutableList<Campsite>?

    suspend fun createCampsite(campsite: Campsite)

    suspend fun editCampsite(campsiteId: String, campsite: Campsite)

    suspend fun getCampsite(campsiteId: String): Campsite?

    suspend fun queryCampsites(query: String): MutableList<Campsite>?

    fun observeCampsite(campsiteId: String): Flow<Campsite?>
}