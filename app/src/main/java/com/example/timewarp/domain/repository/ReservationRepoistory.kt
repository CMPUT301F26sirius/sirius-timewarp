package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Reservation
import kotlinx.coroutines.flow.Flow

interface ReservationRepository {
    fun getReservations(): Flow<List<Reservation>>
}