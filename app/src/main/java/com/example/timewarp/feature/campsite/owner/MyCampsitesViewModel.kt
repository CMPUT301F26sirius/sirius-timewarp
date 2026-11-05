package com.example.timewarp.feature.campsite.owner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.usecase.CheckUpcomingLotteryUseCase
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Loads the signed-in owner's campsites for the My Sites tab (US 02.03.01).
 * Outstanding: active bookings per campsite not shown yet (needs a reservations-by-campsite query).
 */

/**
 * One card on the My Sites tab: a campsite and whether it has an upcoming lottery.
 **/
data class MyCampsiteItem(
    val campsite: Campsite,
    val hasUpcomingLottery: Boolean
)

/** What the My Sites tab is showing. */
sealed interface MyCampsitesState {
    data object Loading : MyCampsitesState
    data class Content(val items: List<MyCampsiteItem>) : MyCampsitesState
    data class Error(val message: String) : MyCampsitesState
}

/** ViewModel for the My Sites tab. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MyCampsitesViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val checkUpcomingLottery: CheckUpcomingLotteryUseCase,
    observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<MyCampsitesState>(MyCampsitesState.Loading)
    val state: StateFlow<MyCampsitesState> = _state.asStateFlow()
    val isOwner: StateFlow<Boolean> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                false
            } else if (user.userType == UserType.CampsiteOwner) {
                true
            } else if (user.userType == UserType.Admin) {
                true
            } else {
                false
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        observe()
    }

    // Keeps the list in sync with the signed-in owner's campsites
    private fun observe() {
        viewModelScope.launch {
            authRepository.currentUserId
                .flatMapLatest { userId ->
                    // Signed out: nothing to show
                    if (userId == null) {
                        flowOf(emptyList())
                    } else {
                        campsiteRepository.observeOwnerCampsites(userId)
                    }
                }
                .map { campsites -> loadItems(campsites) }
                .catch { error ->
                    _state.value = MyCampsitesState.Error(
                        error.message ?: "Unable to load your campsites."
                    )
                }
                .collect { items ->
                    _state.value = MyCampsitesState.Content(items)
                }
        }
    }

    // Pairs each campsite with whether it has an upcoming lottery
    private suspend fun loadItems(campsites: List<Campsite>): List<MyCampsiteItem> {
        val items = mutableListOf<MyCampsiteItem>()
        for (campsite in campsites) {
            val lotteries = lotteryRepository.observeCampsiteLotteries(campsite.campsiteId).first()
            val upcoming = checkUpcomingLottery(lotteries, today())
            items.add(MyCampsiteItem(campsite, upcoming))
        }
        return items
    }

    // Today's day number (days since Jan 1, 1970)
    private fun today(): Int {
        val millisecondsPerDay = 24L * 60 * 60 * 1000
        return (System.currentTimeMillis() / millisecondsPerDay).toInt()
    }
}