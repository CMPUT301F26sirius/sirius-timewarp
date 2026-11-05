package com.example.timewarp.feature.lottery.create

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.timewarp.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update

sealed interface CreateLotteryState {
    data object Loading : CreateLotteryState
    data class Content(
        val campsites: List<Campsite>,
        val selectedCampsites: List<Campsite>,
        val currentUserProfile: Profile?,
        val currentUser: User?
    ) : CreateLotteryState
    data class Details(
        val selectedCampsites: List<Campsite>
    ) : CreateLotteryState

    data class Error(val message: String) : CreateLotteryState
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<CreateLotteryState>(
        CreateLotteryState.Loading
    )
    val state: StateFlow<CreateLotteryState> = _state.asStateFlow()

    private val _selectedCampsites = MutableStateFlow<List<Campsite>>(emptyList())
    private  val _chosenCampsites = MutableStateFlow<List<Campsite>?>(null)

    var winnerCount by mutableStateOf<Int?>(Lottery().winnerCount)
    var waitlistCount by mutableStateOf<Int?>(Lottery().waitlistCapacity)

    var registrationPeriod by mutableStateOf<Period?>(null)
    var bookingPeriod by mutableStateOf<Period?>(null)

    init {
        lotteryCreation()
    }

    fun toggleCampsite(campsite: Campsite) {
        _selectedCampsites.update { selected ->
            if (campsite in selected) {
                selected - campsite
            } else {
                selected + campsite
            }
        }
    }

    fun goToDetailSelection() {
        val content = _state.value as? CreateLotteryState.Content ?: return
        if (content.selectedCampsites.isEmpty()) return
        _chosenCampsites.value = content.selectedCampsites
    }

    fun goToCampsiteSelection() {
        if (isCreating) return
        _chosenCampsites.value = null
    }

    fun onWinnerCountChange(text: String) {
        winnerCount = text.toIntOrNull()
    }

    fun onWaitListCountChange(text: String) {
        waitlistCount = text.toIntOrNull()
    }

    fun onRegistrationPeriodChange(period: Period?) {
        registrationPeriod = period
    }

    fun onBookingPeriodChange(period: Period?) {
        bookingPeriod = period
    }

    private fun lotteryCreation() {
        viewModelScope.launch {
            _state.value = CreateLotteryState.Loading

            observeCurrentUser()
                .flatMapLatest { currentUser ->
                    if (currentUser == null) {
                        flowOf<CreateLotteryState>(
                            // User ID not found, set to Error
                            CreateLotteryState.Error("Unable to fetch user")
                        )
                    } else {
                        // Get filtered campsites and user's profile to set to content
                        val ownedCampsites = getUsersOwnedCampsites(currentUser.userId).orEmpty()
                        combine(
                            observeUserProfile(currentUser.userId),
                            _selectedCampsites,
                            _chosenCampsites
                        ) {
                            currentUserProfile, selected, chosenCampsites ->
                            if (chosenCampsites != null &&
                                chosenCampsites.all {it.ownerUserId == currentUser.userId}
                                ) {
                                CreateLotteryState.Details(
                                    selectedCampsites = chosenCampsites
                                )
                            } else {
                                CreateLotteryState.Content(
                                    campsites = ownedCampsites,
                                    selectedCampsites = ownedCampsites.filter { it in selected },
                                    currentUser = currentUser,
                                    currentUserProfile = currentUserProfile?.profile,
                                )
                            }
                        }

                    }
                }
                // Campsites not found, set to Error
                .catch { error ->
                    _state.value = CreateLotteryState.Error(
                        error.message ?: "Unable to fetch campsite lottery details"
                    )
                }
                .collect { _state.value = it }
        }
    }

    var createError by mutableStateOf<String?>(null)
    var isCreating by mutableStateOf(false)


    fun createLotteryInDB(onCreated: () -> Unit) {
        val details = _state.value as? CreateLotteryState.Details?: return
        val winners = winnerCount?: return
        val waitlist = waitlistCount?: return
        val registration = registrationPeriod?: return
        val booking = bookingPeriod?: return
        if (isCreating || winners < 1 || waitlist < 0 ) return

        isCreating = true
        createError = null

        viewModelScope.launch {

            try {
                lotteryRepository.createLottery(
                    Lottery(
                        registrationPeriod = registration,
                        bookingPeriod = booking,
                        winnerCount = winners,
                        waitlistCapacity = waitlist,
                        campsiteIds = details.selectedCampsites.map { it.campsiteId }
                    )
                )
            } catch (error: Exception) {
                createError = error.message?: "Failed to create lottery"
                isCreating = false
                return@launch
            }
            onCreated()
        }
    }

    // Get all campsites, filter where owner ID is same as current user's ID
    private suspend fun getUsersOwnedCampsites(userId: String): List<Campsite>? {
        return campsiteRepository.getCampsites()?.filter { it.ownerUserId == userId }
    }
}