package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Eric_Reservation
import kotlinx.coroutines.flow.Flow

interface Eric_ReservationRepository {

    fun getReservation(id: String): Eric_Reservation?
    fun getReservations(): Flow<List<Eric_Reservation>>

}