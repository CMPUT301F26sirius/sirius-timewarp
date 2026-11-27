package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import javax.inject.Inject

/**
 * This class is for deleting a user's account and profile. It keeps a record of past reservations
 * and rating form the profile being deleted. Active reservations linked to the profile being
 * deleted will be canceled and all the rating from that profile will be assigned as "Deleted User"
 *
 * @param userRepository used to delete the user account record
 * @param profileRepository used to delete the user's profile data
 * @param reservationRepository used to cancel reservations
 * @param ratingRepository used to resave ratings under a placeholder user ID
 */
class DeleteUserProfileUseCase @Inject constructor(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val reservationRepository: ReservationRepository,
    private val ratingRepository: RatingRepository,
) {
    /**
     * This function run the profile deletion for the selected user
     *
     * @param userId
     */
    suspend operator fun invoke(userId: String) {
        reservationRepository.getReservations().orEmpty().filter { it.userId == userId }
            .forEach {
                reservation ->
                val isActive = reservation.reservationState in activeState
                if (isActive) reservationRepository.cancelReservation(
                    reservation.reservationId)
                reservationRepository.editReservation(
                    reservation.reservationId, reservation.copy(
                        userId = DELETED_USER,
                        reservationState = if (isActive) ReservationState.Canceled
                        else reservation.reservationState
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

        const val DELETED_USER = "deleted-user"
    }
}