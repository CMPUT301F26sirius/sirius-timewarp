package com.example.timewarp.feature.campsite.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.startup.StartupScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * [CampsiteDetailsState] State of the ui
 */
sealed interface CampsiteDetailsState {
    data object Loading : CampsiteDetailsState
    data class Content(
        val campsite: Campsite,
        val ratings: List<Rating>,
        val lotteries: List<Lottery>,
        val ownerUser: User?,
        val ownerProfile: Profile?,
        val currentUser: User?
    ) : CampsiteDetailsState {

        // Current logged-in user's own rating for the campsite. If they did not rate then null
        val myRating: Rating?
            get() = ratings.firstOrNull { it.userId == currentUser?.userId }

        // Show 0.0 for no rating. If there are ratings then average star across all ratings
        val averageRating: Double
            get() = if (ratings.isEmpty()) 0.0
            else ratings.map {it.rating}.average()

        val ratingCount: Int
            get() = ratings.size

        // The entrant can rate the campsite
        val canRate: Boolean
            get() = currentUser?.userType == UserType.Entrant
    }

    data class Error(val message: String) : CampsiteDetailsState
}

/**
 * [CampsiteDetailsViewModel] for the [StartupScreen].
 *
 * Loads the details for the campsite, including ratings, lotteries
 * and owner info.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CampsiteDetailsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<CampsiteDetailsState>(CampsiteDetailsState.Loading)
    val state: StateFlow<CampsiteDetailsState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(campsiteId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            _state.value = CampsiteDetailsState.Loading

            campsiteRepository.observeCampsite(campsiteId)
                .flatMapLatest { campsite ->
                    if (campsite == null) {
                        // Campsite not found, set state to error.
                        flowOf<CampsiteDetailsState>(
                            CampsiteDetailsState.Error("Campsite not found.")
                        )
                    } else {
                        // Combine all flows to update the state
                        combine(
                            flowOf(campsite),
                            ratingRepository.observeCampsiteRatings(campsiteId),
                            lotteryRepository.observeCampsiteLotteries(campsiteId),
                            observeCurrentUser(),
                            observeUserProfile(campsite.ownerUserId)
                        ) { campsite, ratings, lotteries, currentUser, ownerUserProfile ->
                            CampsiteDetailsState.Content(
                                campsite = campsite,
                                ratings = ratings,
                                lotteries = lotteries.filter { lottery ->
                                    val today =
                                        TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis()).toInt()
                                    today in lottery.registrationPeriod.start..lottery.registrationPeriod.end
                                },
                                ownerUser = ownerUserProfile?.user,
                                ownerProfile = ownerUserProfile?.profile,
                                currentUser = currentUser
                            )
                        }
                    }
                }
                .catch { error ->
                    _state.value = CampsiteDetailsState.Error(
                        error.message ?: "Unable to load campsite details."
                    )
                }
                .collect { _state.value = it }
        }
    }

    /**
    * This function posts or update the signed in entrant's rating.
     *
     * @param stars This is the Star rating. you can give rating from 1 to 5 stars
     * @param review This is the review text.
    */

    fun onSubmitRating(stars: Int, review: String) {
        val content = _state.value as? CampsiteDetailsState.Content ?: return

        // User who is not an Entrant cannot submit
        val user = content.currentUser?: return
        if (user.userType != UserType.Entrant) return

        if (stars !in 1..5) return
        val userId = user.userId

        // The user needs to be signed in to rate a campsite
        val campsiteId = content.campsite.campsiteId
        viewModelScope.launch {
            ratingRepository.createRating(
                Rating(
                    ratingId = "${campsiteId}_$userId",
                    campsiteId = campsiteId,
                    userId = userId,
                    rating = stars,
                    review = review.trim()
                )
            )
        }
    }
}