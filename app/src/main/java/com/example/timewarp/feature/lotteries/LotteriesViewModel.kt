package com.example.timewarp.feature.lotteries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.feature.reservations.ReservationItem
import com.example.timewarp.feature.reservations.ReservationTab
import com.example.timewarp.feature.reservations.ReservationsState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.collections.emptyList

/**
 * Responsible for holding all the text that can change whenever
 * the user switches between tabs.
 *
 * @param label Text shown on the tab chip
 * @param heading Heading shown above the list
 * @param emptyHeading Heading shown when the list has no value
 * @param states states each tab can hold
 */

sealed class LotteryTab(
    val label: String,
    val heading: String,
    val emptyHeading: String,
    val states: Set<ReservationState>,
) {
    // Lotteries the user won but hasn't booked yet
    data object Entered : LotteryTab(
        label = "Entered",
        heading = "You Won!",
        emptyHeading = "Nothing booked yet!",
        states = setOf(ReservationState.Pending),
    )

    // Lotteries te user is waiting on
    data object Waitlisted : LotteryTab(
        label = "Booking",
        heading = "There is still a chance!",
        emptyHeading = "You haven't booked yet!",
        states = setOf(ReservationState.Waitlisted),
    )
}

/**
 * Responsible for holding only what each card is supposed to show.
 *
 *
 * @param lotteryId Used to open the lottery when the card is tapped
 * @param state which tab the card appears in
 * @param title Campsite name
 * @param subtitle Winners and waitlist size
 * @param dateText text such as "Won 2 days ago"
 */
data class LotteryItem(
    val lotteryId: String,
    val state: ReservationState,
    val title: String,
    val subtitle: String,
    val dateText: String,
)

// Everything the lotteries can show
sealed interface LotteriesState {

    data object Loading : LotteriesState

    /**
     * Responsible for making sure that lotteries load successfully or if they are empty.
     *
     * @param selectedTab: the tab which the user currently clicked
     * @param items: Every lottery for this tab
     */
    data class Content(
        val selectedTab: LotteryTab = LotteryTab.Entered,
        val items: List<LotteryItem> = emptyList(),
    ) : LotteriesState {
        // Only the reservations belonging to the current tab (selectedTab)
        val visibleItems: List<LotteryItem>
            get() = items.filter { it.state in selectedTab.states }
    }

    // Could not load, send error
    data class Error(val message: String) : LotteriesState
}

/**
 * Responsible for loading lotteries that each mode can see, combines the campsite and turns
 * it into a card.
 *
 * @param campsiteRepository campsite information (mainly used for campsite name)
 * @param lotteryRepository lotteries information
 * @param reservationRepository Reservation information
 * @param observeCurrentUser Signed-in userId
 */
@HiltViewModel
class LotteriesViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val lotteryRepository: LotteryRepository,
    private val reservationRepository: ReservationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow<LotteriesState>(LotteriesState.Loading)

    // Current Screen State
    val state: StateFlow<LotteriesState> = _state.asStateFlow()

    /**
     * Tabs for the current user, updated whenever the signed-in user or their type changes.
     * Empty while no one is signed in.
     */
    val tabs: StateFlow<List<LotteryTab>> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                emptyList()
            } else {
                when (user.userType) {
                    UserType.Entrant -> listOf(
                        LotteryTab.Entered,
                        LotteryTab.Waitlisted,
                    )

                    UserType.CampsiteOwner -> listOf(
                        LotteryTab.Entered,
                        LotteryTab.Waitlisted,
                    )

                    UserType.Admin -> listOf(
                        LotteryTab.Entered,
                        LotteryTab.Waitlisted,
                    )
                }
            }
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    /**
     * Responsible for loading the screen. So if the state changes such as a
     * cancellation, these changes appear when coming back to reservations.
     */
    fun load() {
        viewModelScope.launch {

            // Only show "Loading..." the first time; on reload, keep the old list on screen.
            if (_state.value !is LotteriesState.Content) _state.value = LotteriesState.Loading

            try {
                val user = observeCurrentUser().first()
                if (user == null) {
                    _state.value = LotteriesState.Error("Sign in to view lotteries")
                    return@launch
                }

                val allReservations = reservationRepository.getReservations().orEmpty()
                val lotteries = lotteryRepository.getLotteries().orEmpty()

                // The user's lottery entries
                val entries = allReservations
                    .filter { it.userId == user.userId &&
                            (it.reservationState == ReservationState.Pending || it.reservationState
                                    == ReservationState.Waitlisted)}
                    .sortedByDescending { it.timestamp } // newest first

                // An entry belongs to the lottery that includes its campsite
                // Entries with no matching lottery are skipped.
                val items = mutableListOf<LotteryItem>()
                for (entry in entries) {
                    for (lottery in lotteries) {
                        if (entry.campsiteId in lottery.campsiteIds) {
                            val peopleWaiting = allReservations.count {
                                it.campsiteId in lottery.campsiteIds && it.reservationState ==
                                        ReservationState.Waitlisted
                            }

                            val title = lotteryTitle(lottery)

                            items.add(entry.toItem(lottery, title, peopleWaiting))
                            break
                        }
                    }
                }

                _state.value = LotteriesState.Content(
                    selectedTab = LotteryTab.Entered,
                    items = items,
                )
            } catch (e: Exception) {
                // Any failed fetch fails the whole list.
                _state.value = LotteriesState.Error("Unable to load lotteries")
            }
        }
    }

    /**
     * Turns the user's lottery entry into a card.
     *
     * @param lottery The lottery the reservation belongs to.
     * @param title Name to show on the card.
     * @param peopleWaiting How many reservations for this lottery are on the waitlist.
     */
    private fun Reservation.toItem(lottery: Lottery, title: String, peopleWaiting: Int): LotteryItem {
        val daysAgo = daysAgoText(timestamp)
        return LotteryItem(
            lotteryId = lottery.lotteryId,
            state = reservationState,
            title = title,
            subtitle = "${lottery.winnerCount} Winners - $peopleWaiting People Waiting",
            dateText = if (daysAgo.isEmpty()) "" else "Drawn $daysAgo",
        )
    }

    /**
     * Switches tabs
     *
     * @param tab The tab the user clicked
     */
    fun selectTab(tab: LotteryTab) {
        _state.update { (it as? LotteriesState.Content)?.copy(selectedTab = tab) ?: it }
    }

    /**
     * Builds a title for a lottery, since [Lottery] has no name of its own.
     *
     * @param lottery The lottery to name.
     * @return e.g. "Wapiti Camp Lottery", or "Lottery" if its first campsite can't be found.
     */
    private suspend fun lotteryTitle(lottery: Lottery): String {
        val firstCampsiteId = lottery.campsiteIds.firstOrNull() ?: return "Lottery"
        val campsiteName = campsiteRepository.getCampsite(firstCampsiteId)?.name
        return if (campsiteName.isNullOrBlank()) "Lottery" else "$campsiteName Lottery"
    }

    /**
     * Formatting how long ago a timestamp was
     *
     * @param epochSeconds time to format, in seconds
     */
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