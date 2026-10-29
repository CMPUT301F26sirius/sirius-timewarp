package com.example.timewarp.data.repository


import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Entrant
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.repository.ReservationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi

class FakeReservationRepository @Inject constructor(): ReservationRepository {
    @OptIn(ExperimentalUuidApi::class)
    override fun getReservations(): Flow<List<Reservation>> {
        return flowOf(
            listOf(

                Reservation(
                    id = "1",
                    timestamp = Instant.parse("2026-10-28T23:33:00Z"),
                    startDate = "2026-10-06",
                    endDate = "2026-10-07",

                    entrant = Entrant(
                        id = "1",
                        name = "John"
                    ),

                    campsite = Campsite (
                        id = "1",
                        ownerId = "1",
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
}

