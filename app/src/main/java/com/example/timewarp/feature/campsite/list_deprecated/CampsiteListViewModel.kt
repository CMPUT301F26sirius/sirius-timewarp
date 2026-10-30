package com.example.timewarp.feature.campsite.list_deprecated

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class CampsiteListState (
    val campsites: List<Campsite> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CampsiteListViewModel @Inject constructor (
    private val campsiteRepository: CampsiteRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CampsiteListState())
    val uiState = _uiState.asStateFlow()


//    // This code will run only when the ViewModel is created
//    // Init will help with UI not calling the ViewModel everytime a user change the screen
//    init {
//        viewModelScope.launch {
//            campsiteRepository.getCampsites().collect {
//                list -> _uiState.update { it.copy(campsites = list,
//                    isLoading = false) }
//            }
//        }
//    }
}