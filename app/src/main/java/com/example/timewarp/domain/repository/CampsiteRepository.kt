package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite

interface CampsiteRepository {
    suspend fun getCampsites(): MutableList<Campsite>?

    suspend fun createCampsite(campsite: Campsite)

    suspend fun editCampsite(campsiteId: String, campsite: Campsite)

    suspend fun getCampsite(campsiteId: String): Campsite?

    suspend fun queryCampsites(query: String): MutableList<Campsite>?
}