package com.example.timewarp.data.repository


import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Eric_Entrant
import com.example.timewarp.domain.model.Eric_Reservation
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.Eric_ReservationRepository
import com.example.timewarp.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

@Singleton
class Eric_MockReservationRepository @Inject constructor(

): Eric_ReservationRepository {
    override fun getReservations(): Flow<List<Eric_Reservation>> {
        return flowOf(
            listOf(
                Eric_Reservation(
                    reservationId = "1",
                    timestamp = Instant.parse("2026-10-28T23:33:00Z"),
                    startDate = "2026-10-06",
                    endDate = "2026-10-07",

                    entrant = Eric_Entrant(
                        id = "1",
                        name = "John"
                    ),

                    campsite = Campsite(
                        campsiteId = "1",
                        ownerUserId = "1",
                        name = "Fake Campsite",
                        description = "",
                        location = Location(
                            address = "The Moon",
                            latitude = 0.0,
                            longitude = 0.0,
                        ),
                    )
                )
            )
        )
    }

    override fun getReservation(id: String): Eric_Reservation? {
        TODO("Not yet implemented")
    }
}

