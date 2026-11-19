package com.example.timewarp.feature.lottery.waitlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.data.repository.FirebaseReservationRepository
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.lottery.details.LotteryDetailsState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [WaitlistState] State of the ui
 */
sealed interface WaitlistState {

    data object Loading : WaitlistState

    data class Content(
        val lotteryID: String,
    ): WaitlistState

    data class Error(val message: String): WaitlistState

}



/**
 * [WaitlistViewModel] for the [WaitlistScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WaitlistViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository,
    private val reservationRepository: ReservationRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<WaitlistState>(WaitlistState.Loading)
    val state: StateFlow<WaitlistState> = _state.asStateFlow()

    // Current launched job
    private var observationJob: Job? = null

    private var lotteryId: String = ""
    private var loadLottery: Lottery? = null
    private var userId: String = ""

    // Observes the supplied lottery ID. Called by screen when changed.
    fun observe(lotteryId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            if (_state.value !is WaitlistState.Content) _state.value = WaitlistState.Loading

            observeCurrentUser()
                .catch { _state.value = WaitlistState.Error("Unable to load lottery") }
                .collect { user ->
                    if (user == null) {
                        _state.value = WaitlistState.Error("Unable to load user")
                    } else {
                        try {
                            _state.value = loadContent(lotteryId, user)
                        } catch (e: Exception) {
                            _state.value = WaitlistState.Error("Unable to load lottery")
                        }
                    }
                }
        }
    }

    private suspend fun loadContent(lotteryId: String, user: User) {
        val reservations = getWaitlistForLottery(lotteryId)

        // TODO: THIS!
    }

    private suspend fun getWaitlistForLottery(lotteryId: String): List<Reservation> {
        val reservations = reservationRepository.getReservations()
        return reservations?.filter {it.lotteryId == lotteryId } ?: emptyList()
    }
}