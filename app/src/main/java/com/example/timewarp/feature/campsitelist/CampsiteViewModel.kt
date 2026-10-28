package com.example.timewarp.feature.campsitelist

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.ExampleModel
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ExampleRepository
import com.example.timewarp.feature.example.ExampleState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CampsiteListState (
    val campsite: List<Campsite> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CampsiteViewModel @Inject constructor (
    private val campsiteRepository: CampsiteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampsiteListState())
    val uiState = _uiState.asStateFlow()


}