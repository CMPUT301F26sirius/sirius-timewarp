package com.example.timewarp.feature.campsite.booked

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.usecase.BookedStay
import com.example.timewarp.domain.usecase.FindBookedStaysUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/*
 * Loads the booked stays at one campsite for its owner (US 02.06.01).
 * Outstanding: shows reservation numbers, not entrant names.
 */

/** What the Booked dates screen shows: loading, the bookings, or an error. */
sealed interface BookedDatesState {
    data object Loading : BookedDatesState

    /**
     * The campsite and its current and upcoming booked stays.
     *
     * @param campsite the campsite, or null if it couldn't be loaded.
     * @param stays booked stays, earliest first.
     */
    data class Content(
        val campsite: Campsite?,
        val stays: List<BookedStay>
    ) : BookedDatesState

    data class Error(val message: String) : BookedDatesState
}

/** ViewModel for the Booked dates screen. */
@HiltViewModel
class BookedDatesViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val findBookedStays: FindBookedStaysUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<BookedDatesState>(BookedDatesState.Loading)
    val state: StateFlow<BookedDatesState> = _state.asStateFlow()

    /** Loads the campsite with [campsiteId] and its booked stays from today on. */
    fun load(campsiteId: String) {
        viewModelScope.launch {
            _state.value = BookedDatesState.Loading
            try {
                // An empty id isn't a valid document, so skip the lookup
                var campsite: Campsite? = null
                if (campsiteId.isNotEmpty()) {
                    campsite = campsiteRepository.getCampsite(campsiteId)
                }

                // Stays that ended before today aren't "currently booked"
                val today = DayFormatter.fromMillis(System.currentTimeMillis())
                val stays = findBookedStays(campsiteId, today)

                _state.value = BookedDatesState.Content(campsite = campsite, stays = stays)
            } catch (error: Exception) {
                _state.value = BookedDatesState.Error(
                    error.message ?: "Unable to load booked dates."
                )
            }
        }
    }
}