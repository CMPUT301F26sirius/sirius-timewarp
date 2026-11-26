package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import javax.inject.Inject

class DeleteUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val reservationRepository: ReservationRepository,
    private val ratingRepository: RatingRepository,
) {
    suspend operator fun invoke(userId: String) {
        reservationRepository.getReservations().orEmpty().filter { it.userId == userId }
            .forEach {
                reservation ->
                val newState = if (
                    reservation.reservationState in activeState
                    ) ReservationState.Canceled
                else reservation.reservationState

                reservationRepository.editReservation(
                    reservation.reservationId,
                    reservation.copy(
                        userId = DELETED_USER, reservationState = newState
                    )
                )
            }
        ratingRepository.getUserRatings(userId).orEmpty()
            .forEach {
                rating ->
                ratingRepository.createRating(
                    rating.copy(userId = DELETED_USER)
                )
            }

        profileRepository.deleteProfile(userId)
        userRepository.deleteUser(userId)
    }

    companion object {
        private val activeState = setOf(
            ReservationState.Waitlisted,
            ReservationState.Pending,
            ReservationState.Reserved
        )

        const val DELETED_USER = "Deleted-user"
    }
}