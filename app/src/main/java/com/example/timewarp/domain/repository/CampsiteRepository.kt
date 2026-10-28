package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Campsite
import kotlinx.coroutines.flow.Flow

interface CampsiteRepository {

    fun getCampsites(): Flow<List<Campsite>>
}