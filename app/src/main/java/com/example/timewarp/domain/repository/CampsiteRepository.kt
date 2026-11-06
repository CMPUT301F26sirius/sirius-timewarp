package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * [AuthRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface CampsiteRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getCampsites(): MutableList<Campsite>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun createCampsite(campsite: Campsite)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun editCampsite(campsiteId: String, campsite: Campsite)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getCampsite(campsiteId: String): Campsite?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun queryCampsites(query: String): MutableList<Campsite>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    fun observeCampsite(campsiteId: String): Flow<Campsite?>

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun filterCampsites(filter: CampsiteFilter): MutableList<Campsite>?
}