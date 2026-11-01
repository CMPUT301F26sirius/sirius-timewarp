package com.example.timewarp.feature.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.FilterCampsite
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.model.matches
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface RegistrationState {
    data object Idle : RegistrationState
}

data class BrowseState(
    val campsites: List<Campsite> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filters: FilterCampsite = FilterCampsite()
)
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    //private val _state = MutableStateFlow<RegistrationState>(RegistrationState.Idle)
    //val state: StateFlow<RegistrationState> = _state.asStateFlow()
    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state.asStateFlow()
    private var allCampsite: List<Campsite> = emptyList()


    init {
        loadCampsites()
    }

    fun loadCampsites() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val list = campsiteRepository.getCampsites()

                allCampsite = list.orEmpty()
                _state.update{
                    it.copy(campsites = allCampsite.filter{ campsite-> campsite.matches(it.filters)},
                        isLoading = false
                    )
                }
            }
            catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Error. Failed To Load Campsites"
                    )
                }
            }
        }
    }

    fun onFiltersChanged(filters: FilterCampsite) {
        _state.update {
            it.copy(
                filters = filters,
                campsites = allCampsite.filter { campsite-> campsite.matches(filters) }
            )
        }
    }
}