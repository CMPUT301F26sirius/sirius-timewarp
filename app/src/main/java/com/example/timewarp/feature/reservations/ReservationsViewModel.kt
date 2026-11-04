package com.example.timewarp.feature.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

private val TEST_USER_ID: String? = "test-user"

sealed class ReservationTab(
    val label: String,
    val heading: String,
    val emptyHeading: String,
    val cardSubtitle: String,
    val datePrefix: String,
) {
    data object Booked : ReservationTab(
        label = "Booked",
        heading = "Get ready to go!",
        emptyHeading = "Nothing booked yet!",
        cardSubtitle = "Tap to manage your reservation",
        datePrefix = ""
    )

    data object Booking : ReservationTab(
        label = "Booking",
        heading = "There is still a chance!",
        emptyHeading = "You haven't booked yet!",
        cardSubtitle = "Make your reservation now!",
        datePrefix = ""
    )

    data object Past : ReservationTab(
        label = "Past",
        heading = "Trips you've taken",
        emptyHeading = "No past trips yet!",
        cardSubtitle = "Hope you had a great trip",
        datePrefix = ""
    )

    data object Cancelled : ReservationTab(
        label = "Cancelled",
        heading = "Cancelled bookings",
        emptyHeading = "Nothing cancelled!",
        cardSubtitle = "This booking was cancelled",
        datePrefix = ""
    )
}

val reservationTabs: List<ReservationTab>
    get() = listOf(ReservationTab.Booked, ReservationTab.Booking, ReservationTab.Past, ReservationTab.Cancelled)

fun ReservationState.toTab(): ReservationTab = when (name.lowercase()) {
    "booked", "confirmed", "reserved", "accepted" -> ReservationTab.Booked
    "completed", "past", "finished", "expired" -> ReservationTab.Past
    "cancelled", "canceled", "declined", "rejected" -> ReservationTab.Cancelled
    else -> ReservationTab.Booking
}
data class ReservationItem(
    val reservationId: String,
    val tab: ReservationTab,
    val title: String,
    val dateText: String,
)

sealed interface ReservationsState {
    data object Loading : ReservationsState

    data class Content(
        val selectedTab: ReservationTab = ReservationTab.Booking,
        val items: List<ReservationItem> = emptyList(),
    ) : ReservationsState {
        // Only the reservations belonging to [selectedTab]
        val visibleItems: List<ReservationItem>
            get() = items.filter { it.tab == selectedTab }
    }

    /** Something went wrong; [message] is shown to the user. */
    data class Error(val message: String) : ReservationsState
}


@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val reservationRepository: ReservationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationsState>(ReservationsState.Loading)
    val state: StateFlow<ReservationsState> = _state.asStateFlow()

    // TEMP sample data. Canceled is left empty on purpose to show the empty view
    private val sampleItems = listOf(
        ReservationItem("1", ReservationTab.Booking, "You Won Super Cool Lottery!", "Drawn 3 days ago"),
        ReservationItem("2", ReservationTab.Booking, "You Won Super Cool Lottery!", "Drawn 3 days ago"),
        ReservationItem("3", ReservationTab.Booked, "Lakeside Pitch", "2 days ago"),
        ReservationItem("4", ReservationTab.Past, "Pine Ridge Camp", "40 days ago"),
    )

    fun load() {

        viewModelScope.launch {
            val selectedTab = (_state.value as? ReservationsState.Content)?.selectedTab ?: ReservationTab.Booking
            if (_state.value !is ReservationsState.Content) _state.value = ReservationsState.Loading

            try {
                val userId = TEST_USER_ID ?: observeCurrentUser().first()?.userId
                if (userId == null) {
                    _state.value = ReservationsState.Error("Sign in to view reservations")
                    return@launch
                }

                val reservations = reservationRepository.getReservations()
                    .orEmpty()
                    .filter { it.userId == userId }
                    .sortedByDescending { it.timestamp }

                val campsiteNames = reservations
                    .map { it.campsiteId }
                    .distinct()
                    .associateWith { campsiteId -> campsiteRepository.getCampsite(campsiteId)?.name }

                val items = reservations.map { it.toItem(campsiteNames[it.campsiteId]) }

                _state.value = ReservationsState.Content(selectedTab = selectedTab, items = items)
            } catch (e: Exception) {
                _state.value = ReservationsState.Error("Unable to load reservations")
            }
        }
    }

    private fun Reservation.toItem(campsiteName: String?): ReservationItem {
        val tab = reservationState.toTab()
        val daysAgo = daysAgoText(timestamp)
        return ReservationItem(
            reservationId = reservationId,
            tab = tab,
            title = campsiteName?.takeIf { it.isNotBlank() } ?: "Campsite",
            dateText = if (daysAgo.isEmpty()) "" else tab.datePrefix + daysAgo,
        )
    }

    fun selectTab(tab: ReservationTab) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedTab = tab) ?: it }
    }

    private fun daysAgoText(epochSeconds: Int): String {
        if (epochSeconds <= 0) return ""

        val elapsedMillis = System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(epochSeconds.toLong())
        val days = TimeUnit.MILLISECONDS.toDays(elapsedMillis).coerceAtLeast(0)

        return when (days) {
            0L -> "Today"
            1L -> "1 day ago"
            else -> "$days days ago"
        }
    }

}