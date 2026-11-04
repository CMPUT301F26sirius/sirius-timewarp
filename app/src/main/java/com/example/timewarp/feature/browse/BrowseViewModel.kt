package com.example.timewarp.feature.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CampsiteFilter
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.RatingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/*
* This file hold the state and logic for the Browse Screen.
* The code loads campsites and apply search text and filters
*/
data class BrowseState(
    val campsites: List<Campsite> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val filters: CampsiteFilter = CampsiteFilter()
)
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository
) : ViewModel() {
    private val _state = MutableStateFlow(BrowseState())
    val state: StateFlow<BrowseState> = _state.asStateFlow()
    private var filterJob: Job? = null

    init {
        loadCampsites()
    }

    fun loadCampsites() {
        onFiltersChanged(state.value.filters)
    }

    fun onFiltersChanged(newFilters: CampsiteFilter) {
        filterJob?.cancel()
        filterJob = viewModelScope.launch {
            _state.update { it.copy(filters = newFilters, isLoading = true, error = null) }
            try {
                val list = campsiteRepository.filterCampsites(newFilters).orEmpty()
                _state.update { it.copy(campsites = list, isLoading = false) }
            }
            catch (error: CancellationException) {
                throw error
            }
            catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message?: "An Error Occurred. Failed to Load Campsites") }
            }
        }
    }

    fun onSearch(query: String) =
        onFiltersChanged(state.value.filters.copy(query = query.trim()))

    fun onApplyFilters(filter: CampsiteFilter) = onFiltersChanged(filter.copy(query = state.value.filters.query))
}