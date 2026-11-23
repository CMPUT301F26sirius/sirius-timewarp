// Parts of this code were made with the assistance of Claude AI
// While no functionally was implemented wholly by the AI
// I prompted it with questions about how to do certain things in Kotlin/Android
// Or assistance in debugging

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

import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.RatingRepository

import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.feature.lottery.form.LotteryFormState
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.mapLatest

sealed interface CreateLotteryState {
    data object Loading : CreateLotteryState
    data class Content(
        val campsites: List<Campsite>,
    ) : CreateLotteryState

    data class Error(val message: String) : CreateLotteryState
}

/**
 * [CreateLotteryViewModel] for the [CreateLotteryScreen]
 *
 * @param campsiteRepository to get user's owned Campsites
 * @param lotteryRepository to save newly created lottery to
 * @param observeCurrentUser used to get current user
 *
 *
 * @property createError Error raised if Lottery Creation Failed
 * @property isCreating Used to flag current creation process and disable some navigation.
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<CreateLotteryState>(
        CreateLotteryState.Loading
    )
    val state: StateFlow<CreateLotteryState> = _state.asStateFlow()

    val form = LotteryFormState()
    var createError by mutableStateOf<String?>(null)
    var isCreating by mutableStateOf(false)
    // Current launched job
    private var observationJob: Job? = null

    init {
        lotteryCreation()
    }

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(lotteryId: String) {
        // Cancel old launch
        observationJob?.cancel()

        // Start new launch
        observationJob = viewModelScope.launch {
            _state.value = CreateLotteryState.Loading
        }
    }

    private fun lotteryCreation() {
        viewModelScope.launch {
            _state.value = CreateLotteryState.Loading

            observeCurrentUser()
                .mapLatest { currentUser ->
                    if (currentUser == null) {
                        CreateLotteryState.Error("Unable to fetch user")
                    } else {
                        CreateLotteryState.Content(
                            campsites = getUsersOwnedCampsites(currentUser.userId).orEmpty()
                        )
                    }
                }
                // Campsites not found, set to Error
                .catch { error ->
                    _state.value = CreateLotteryState.Error(
                        error.message ?: "Unable to fetch campsite lottery details"
                    )
                }
                .collect { newState ->
                    if (newState is CreateLotteryState.Content) {
                        form.selectedCampsites = newState.campsites.filter { it in form.selectedCampsites }
                    }
                    _state.value = newState
                }
        }
    }




    fun createLotteryInDB(onCreated: () -> Unit) {
        val content = _state.value as? CreateLotteryState.Content ?: return
        val winners = form.winnerCount ?: return
        val waitlist = form.waitlistCount ?: return
        val registration = form.registrationPeriod ?: return
        val booking = form.bookingPeriod ?: return
        if (isCreating || winners < 1 || waitlist < 0) return

        val campsiteIds = content.campsites
            .filter{ it in form.selectedCampsites }
            .map { it.campsiteId }
        if (campsiteIds.isEmpty()) return

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
                        campsiteIds = campsiteIds,
                    )
                )
            } catch (error: Exception) {
                createError = error.message ?: "Failed to create lottery"
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