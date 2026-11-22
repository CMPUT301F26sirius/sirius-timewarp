package com.example.timewarp.feature.lottery.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.lottery.details.LotteryDetailsState
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
    data class Content(
        val lottery: Lottery,
    ): EditLotteryState

    data class Error (val message: String): EditLotteryState
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

    private var lotteryId: String = ""

    private var loadedLottery: Lottery? = null

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(lotteryId: String) {
        this.lotteryId = lotteryId
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            if (_state.value !is EditLotteryState.Content) _state.value = EditLotteryState.Loading

            _state.value = loadContent(lotteryId)
        }
    }

    private suspend fun loadContent(lotteryId: String): EditLotteryState {
        val lottery = lotteryRepository.getLottery(lotteryId)
            ?: return EditLotteryState.Error("Lottery not found")
        return EditLotteryState.Content(
            lottery = lottery
        )
    }
}

