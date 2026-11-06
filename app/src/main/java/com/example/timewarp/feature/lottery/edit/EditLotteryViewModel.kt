package com.example.timewarp.feature.lottery.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [EditLotteryState] State of the ui
 */
sealed interface EditLotteryState {
    data object Loading : EditLotteryState
}

/**
 * [EditLotteryViewModel] for the [EditLotteryScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<EditLotteryState>(EditLotteryState.Loading)
    val state: StateFlow<EditLotteryState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(lotteryId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            _state.value = EditLotteryState.Loading
        }
    }
}