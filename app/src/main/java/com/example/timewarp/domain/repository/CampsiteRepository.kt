package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import kotlinx.coroutines.flow.Flow

@OptIn(kotlin.uuid.ExperimentalUuidApi::class)
interface CampsiteRepository {

    fun getCampsites(): Flow<List<Campsite>>
}