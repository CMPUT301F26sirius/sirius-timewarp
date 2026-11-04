package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.repository.CampsiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [CampsiteRepository].
 *
 * Stores and retrieves campsites from a `campsites` collection.
 */
@Singleton
class FakeCampsiteRepository @Inject constructor() : CampsiteRepository {
    private val campsites = MutableStateFlow<Map<String, Campsite>>(emptyMap())

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsites(): MutableList<Campsite>? {
        return campsites.value.values.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createCampsite(campsite: Campsite) {
        campsites.value = campsites.value + (campsite.campsiteId to campsite)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editCampsite(campsiteId: String, campsite: Campsite) {
        campsites.value = campsites.value + (campsiteId to campsite)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getCampsite(campsiteId: String): Campsite? {
        return campsites.value[campsiteId]
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun queryCampsites(query: String): MutableList<Campsite> {
        return campsites.value.values
            .filter { it.name.contains(query, ignoreCase = true) }
            .toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsite(campsiteId: String): Flow<Campsite?> =
        campsites.map { it[campsiteId] }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun filterCampsites(filter: CampsiteFilter): MutableList<Campsite>? {
        return campsites.value.values
            .filter { campsite ->
                val locationMatches = filter.location.isBlank() ||
                    campsite.location.address.contains(filter.location, ignoreCase = true)
                val guestsMatch = filter.guests == null || campsite.occupancy.maxGuests >= filter.guests
                locationMatches && guestsMatch
            }
            .toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    fun addCampsite(campsite: Campsite) {
        campsites.value = campsites.value + (campsite.campsiteId to campsite)
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    fun clear() {
        campsites.value = emptyMap()
    }
}