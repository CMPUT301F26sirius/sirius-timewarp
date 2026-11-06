package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and querying [Campsite] objects.
 */
interface CampsiteRepository {
    /**
     * @return all campsites, or `null` when no collection result is available.
     */
    suspend fun getCampsites(): MutableList<Campsite>?

    /**
     * @param campsite campsite to create.
     */
    suspend fun createCampsite(campsite: Campsite)

    /**
     * @param campsiteId identifier of the campsite to edit.
     * @param campsite replacement campsite data.
     */
    suspend fun editCampsite(campsiteId: String, campsite: Campsite)

    /**
     * @param campsiteId identifier of the campsite to retrieve.
     * @return the matching campsite, or `null` when it does not exist.
     */
    suspend fun getCampsite(campsiteId: String): Campsite?

    /**
     * @param query text used to search campsite names.
     * @return matching campsites, or `null` when no result is available.
     */
    suspend fun queryCampsites(query: String): MutableList<Campsite>?

    /**
     * @param campsiteId identifier of the campsite to observe.
     * @return a flow emitting the campsite, or `null` when it is unavailable.
     */
    fun observeCampsite(campsiteId: String): Flow<Campsite?>

    /**
     * @param filter filters to apply to the campsite collection.
     * @return campsites matching [filter], or `null` when no result is available.
     */
    suspend fun filterCampsites(filter: CampsiteFilter): MutableList<Campsite>?
}