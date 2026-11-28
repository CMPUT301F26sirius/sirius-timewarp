package com.example.timewarp.feature.lottery.invite

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
import com.example.timewarp.domain.usecase.LotteryDrawPollerUseCase
import com.example.timewarp.feature.lottery.form.LotteryFormState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * [InviteLotteryState] State of the ui
 */
sealed interface InviteLotteryState {
    data object Loading : InviteLotteryState
    data object Content : InviteLotteryState
    data class Error (val message: String): InviteLotteryState
}

/**
 * [InviteLotteryViewModel] for the [InviteLotteryScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class InviteLotteryViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val lotteryDrawPoller: LotteryDrawPollerUseCase,
) : ViewModel() {
    private val _state = MutableStateFlow<InviteLotteryState>(InviteLotteryState.Loading)
    val state: StateFlow<InviteLotteryState> = _state.asStateFlow()

    private var lotteryId: String = ""
}

