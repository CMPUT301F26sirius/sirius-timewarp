package com.example.timewarp.feature.campsite.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface CampsiteDetailsState {
    data object Loading : CampsiteDetailsState
    data class Content(
        val campsite: Campsite,
        val ratings: List<Rating>,
        val lotteries: List<Lottery>,
        val ownerUser: User?,
        val ownerProfile: Profile?,
        val currentUser: User?
    ) : CampsiteDetailsState
    data class Error(val message: String) : CampsiteDetailsState
}

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
                                lotteries = lotteries,
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
}