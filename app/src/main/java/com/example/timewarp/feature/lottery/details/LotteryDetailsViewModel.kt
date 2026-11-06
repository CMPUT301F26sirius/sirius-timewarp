package com.example.timewarp.feature.lottery.details

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
 * [LotteryDetailsState] State of the ui
 */
sealed interface LotteryDetailsState {
    data object Loading : LotteryDetailsState
}

/**
 * [LotteryDetailsViewModel] for the [LotteryDetailsScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LotteryDetailsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<LotteryDetailsState>(LotteryDetailsState.Loading)
    val state: StateFlow<LotteryDetailsState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(lotteryId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            _state.value = LotteryDetailsState.Loading
        }
    }
}