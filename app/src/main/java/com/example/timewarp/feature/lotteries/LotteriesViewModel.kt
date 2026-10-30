package com.example.timewarp.feature.lotteries

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface LotteriesState {
    data object Idle : LotteriesState
}

@HiltViewModel
class LotteriesViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteriesRepository: LotteryRepository
) : ViewModel() {
    private val _state = MutableStateFlow<LotteriesState>(LotteriesState.Idle)
    val state: StateFlow<LotteriesState> = _state.asStateFlow()


}