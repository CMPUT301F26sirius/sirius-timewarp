package com.example.timewarp.feature.lottery.edit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.usecase.LotteryDrawPollerUseCase
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase
import com.example.timewarp.feature.lottery.create.CreateLotteryState
import com.example.timewarp.feature.lottery.details.LotteryDetailsState
import com.example.timewarp.feature.lottery.form.LotteryFormState
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
        val campsites: List<Campsite>,
    ): EditLotteryState

    data class Error (val message: String): EditLotteryState
}

/**
 * [EditLotteryViewModel] for the [EditLotteryScreen].
 *
 * @param campsiteRepository to get user's owned Campsites
 * @param lotteryRepository to save newly created lottery to
 * @param authRepository to get user
 *
 * @property lotteryId the ID for the lottery to be edited
 * @property form An instance of the General Lottery
 * @property saveError Error raised if Lottery Edit Failed
 *
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val lotteryDrawPoller: LotteryDrawPollerUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<EditLotteryState>(EditLotteryState.Loading)
    val state: StateFlow<EditLotteryState> = _state.asStateFlow()


    private var lotteryId: String = ""

    val form = LotteryFormState()

    var saveError by mutableStateOf<String?>(null)

    // Observes the supplied campsite id. Called by screen when changed.
    fun observe(lotteryId: String) {
        if (this.lotteryId == lotteryId) return
        this.lotteryId = lotteryId

        viewModelScope.launch {
            _state.value = try {
                loadContent(lotteryId)
            } catch (e: Exception) {
                EditLotteryState.Error("Unable to load lottery")
            }
        }
    }

    private suspend fun loadContent(lotteryId: String): EditLotteryState {
        val userId = authRepository.getUserId()
            ?: return EditLotteryState.Error("User not found")
        val lottery = lotteryRepository.getLottery(lotteryId)
            ?: return EditLotteryState.Error("Lottery not found")

        val ownedCampsites = campsiteRepository.getCampsites().orEmpty()
            .filter { it.ownerUserId == userId }

        if (ownedCampsites.none {it.campsiteId in lottery.campsiteIds }) {
            return EditLotteryState.Error("You cannot edit this lottery")
        }

        form.fillExistingFields(lottery, ownedCampsites)
        return EditLotteryState.Content(lottery = lottery, campsites = ownedCampsites)
    }
    fun updateLotteryInDB(onSaved: () -> Unit) {
        val content = _state.value as? EditLotteryState.Content ?: return
        val original = content.lottery
        val winners = form.winnerCount ?: return
        val waitlist = form.waitlistCount ?: return
        val registration = form.registrationPeriod ?: return
        val booking = form.bookingPeriod ?: return

        val selectedIds = form.selectedCampsites.map { it.campsiteId }
        val campsiteIds = (original.campsiteIds + selectedIds).distinct().filter { it in selectedIds}

        saveError = null
        viewModelScope.launch {
            try {
                lotteryRepository.editLottery(
                    lotteryId,
                    original.copy(
                        registrationPeriod = registration,
                        bookingPeriod = booking,
                        winnerCount = winners,
                        waitlistCapacity = waitlist,
                        campsiteIds = campsiteIds,
                    )
                )

            } catch (error: Exception) {
                saveError = error.message ?: "Failed to save lottery"
                return@launch
            }

            try {
                lotteryDrawPoller.pollLottery(lotteryId)
            } catch (error: Exception) {
                saveError = "Lottery saved, but its draw could not be checked: ${error.message ?: "Unknown error"}"
                return@launch
            }

            onSaved()
        }
    }
}

