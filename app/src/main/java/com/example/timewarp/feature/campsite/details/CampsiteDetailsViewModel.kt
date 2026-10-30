package com.example.timewarp.feature.campsite.details

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.feature.browse.RegistrationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed interface CampsiteDetailsState {
    data object Idle : CampsiteDetailsState
}

@HiltViewModel
class CampsiteDetailsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow<CampsiteDetailsState>(CampsiteDetailsState.Idle)
    val state: StateFlow<CampsiteDetailsState> = _state.asStateFlow()

}