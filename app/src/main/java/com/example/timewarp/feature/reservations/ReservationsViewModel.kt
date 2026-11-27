package com.example.timewarp.feature.reservations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.core.util.DayFormatter
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
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
import kotlinx.coroutines.Job

/**
 * Responsible for holding all the text that can change whenever
 * the user switches between tabs.
 *
 *
 * @param label Text shown on the tab chip
 * @param heading Heading shown above the list
 * @param emptyHeading Heading shown when the list has no value
 * @param cardSubtitle Small text under each card's title
 * @param datePrefix Text placed before the "_ days ago"
 * @param states states each tab can hold
 * @param showsEndedStays True when ended
 */

sealed class ReservationTab(
    val label: String,
    val heading: String,
    val emptyHeading: String,
    val cardSubtitle: String,
    val datePrefix: String,
    val states: Set<ReservationState>,
    val showsEndedStays: Boolean = false,
) {
    // Reservations that are confirmed/upcoming
    data object Booked : ReservationTab(
        label = "Reserved",
        heading = "Get ready to go!",
        emptyHeading = "Nothing booked yet!",
        cardSubtitle = "Tap to manage your reservation",
        datePrefix = "",
        states = setOf(ReservationState.Reserved),
    )

    // Lottery entries the user has won but has not booked yet
    data object Booking : ReservationTab(
        label = "Pending",
        heading = "There is still a chance!",
        emptyHeading = "You haven't booked yet!",
        cardSubtitle = "Make your reservation now!",
        datePrefix = "",
        states = setOf(ReservationState.Pending),
    )

    // Past reservations
    data object Past : ReservationTab(
        label = "Past",
        heading = "Trips you've taken",
        emptyHeading = "No past trips yet!",
        cardSubtitle = "Hope you had a great trip",
        datePrefix = "",
        states = setOf(ReservationState.Reserved),
        showsEndedStays = true,
    )

    // Any reservations that were canceled either by the user or the owner
    data object Cancelled : ReservationTab(
        label = "Cancelled",
        heading = "Cancelled bookings",
        emptyHeading = "Nothing cancelled!",
        cardSubtitle = "This booking was cancelled",
        datePrefix = "",
        states = setOf(ReservationState.Canceled, ReservationState.Declined, ReservationState.Rejected)
    )
}

/**
 * Responsible for whose reservations to show (entrants vs campsite owners)
 *
 * @param label text shown on chip
 * @param heading Heading shown above a non empty list, or null
 */

sealed class ReservationToDisplay(
    val label: String,
    val heading: String?,
) {
    data object Mine: ReservationToDisplay(
        label = "My Reservations",
        heading = null,
    )

    data object AtMyCamps: ReservationToDisplay(
        label = "Reservations At My Camps",
        heading = "Manage reservations",
    )
}

/**
 * Responsible for the dropdown
 *
 * @param campsiteId campsite ID
 * @param name Name shown in dropdown
 */
data class CampsiteOption(
    val campsiteId: String,
    val name: String,
)

/**
 * Responsible for holding only what each card is supposed to show.
 *
 * @param reservationId Used to open the reservation when the card is tapped
 * @param state which tab the card appears in
 * @param title Campsite name
 * @param dateText text such as "Won 2 days ago"
 * @param canMakeReservation True when the user won and needs to reserve.
 * @param campsiteId Campsite ID
 * @param guestName who made the reservation
 * @param stayText Dates of the stay
 * @param stayLabel "Upcoming", "In Progress", or blank
 * @param checkInDay Check-in as days, used to sort soonest first
 * @param hasEnded True when the stay's end date has passed
 * @param isMine True when the signed-in user made this reservation
 * @param isAtMyCamp True when it's at a campsite the signed-in user owns
 */
data class ReservationItem(
    val reservationId: String,
    val state: ReservationState,
    val title: String,
    val dateText: String,
    val canMakeReservation: Boolean = false,
    val campsiteId: String = "",
    val guestName: String = "",
    val stayText: String = "",
    val stayLabel: String = "",
    val checkInDay: Int = Int.MAX_VALUE,
    val hasEnded: Boolean = false,
    val isMine: Boolean = true,
    val isAtMyCamp: Boolean = false,
)

// interface for everything reservation screen is allowed to show
sealed interface ReservationsState {
    // Reservations that are being fetched (aka loading.)
    data object Loading : ReservationsState

    /**
     * Responsible for making sure that reservations load successfully or if they are empty.
     *
     * @param selectedTab the tab which the user currently clicked
     * @param selectedScope My reservations or reservation at my camps
     * @param selectedCampsiteId Campsite picked in dropdown
     * @param myCampsites Campsites the owner can see, listed in the dropdown
     * @param items Every reservation for this tab
     */
    data class Content(
        val selectedTab: ReservationTab = ReservationTab.Booked,
        val selectedScope: ReservationToDisplay = ReservationToDisplay.Mine,
        val selectedCampsiteId: String? = null,
        val myCampsites: List<CampsiteOption> = emptyList(),
        val items: List<ReservationItem> = emptyList(),
    ) : ReservationsState {
        // Only the reservations belonging to the current tab (selectedTab)
        val visibleItems: List<ReservationItem>
            get() {
                val filtered = items.filter { item ->
                    val inScope = when (selectedScope) {
                        ReservationToDisplay.Mine -> item.isMine
                        ReservationToDisplay.AtMyCamps -> item.isAtMyCamp &&
                                (selectedCampsiteId == null || item.campsiteId == selectedCampsiteId)
                    }
                    val inTab = item.state in selectedTab.states

                    // Reserved stays are split by date: Reserved shows ones still going, Past shows ended ones
                    val rightTime = item.state != ReservationState.Reserved ||
                            item.hasEnded == selectedTab.showsEndedStays

                    inScope && inTab && rightTime
                }

                // Owners see the soonest check-in first; the user's own list stays newest first
                return when (selectedScope) {
                    ReservationToDisplay.Mine -> filtered
                    ReservationToDisplay.AtMyCamps -> filtered.sortedBy { it.checkInDay }
                }
            }
    }
    // Could not load, send error
    data class Error(val message: String) : ReservationsState
    }

/**
 * Responsible for loading reservations that each mode can see, combines the campsite and turns
 * it into a card.
 *
 * @param campsiteRepository campsite information (mainly used for campsite name)
 * @param reservationRepository Reservation information
 * @param profileRepository profile information
 * @param observeCurrentUser Signed-in userId
 */
@HiltViewModel
class ReservationsViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val reservationRepository: ReservationRepository,
    private val profileRepository: ProfileRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ReservationsState>(ReservationsState.Loading)

    // Current Screen State
    val state: StateFlow<ReservationsState> = _state.asStateFlow()

    private var observationJob: Job? = null

    /**
     * Tabs for the current user, updated whenever the signed-in user or their type changes.
     * Empty while no one is signed in.
     */
    val tabs: StateFlow<List<ReservationTab>> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                emptyList()
            } else {
                when (user.userType) {
                    UserType.Entrant -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
                    )

                    UserType.CampsiteOwner -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
                    )

                    UserType.Admin -> listOf(
                        ReservationTab.Booked,
                        ReservationTab.Booking,
                        ReservationTab.Past,
                        ReservationTab.Cancelled,
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
     * Chips for the current user. Entrants get none (they only have their own
     * reservations); owners and admins can also own campsites, so they get both.
     */
    val scopes: StateFlow<List<ReservationToDisplay>> = observeCurrentUser()
        .map { user ->
            if (user == null) {
                emptyList()
            } else {
                when (user.userType) {
                    UserType.Entrant -> emptyList()
                    UserType.CampsiteOwner -> listOf(ReservationToDisplay.Mine, ReservationToDisplay.AtMyCamps)
                    UserType.Admin -> listOf(ReservationToDisplay.Mine, ReservationToDisplay.AtMyCamps)
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

        observationJob?.cancel()


        observationJob = viewModelScope.launch {

            // Only shows "Loading..." at the beginning
            if (_state.value !is ReservationsState.Content) _state.value = ReservationsState.Loading

            try {
                val user = observeCurrentUser().first()
                if (user == null) {
                    _state.value = ReservationsState.Error("Sign in to view reservations")
                    return@launch
                }

                val myCampsites = campsiteRepository.getCampsites().orEmpty()
                    .filter { it.ownerUserId == user.userId }
                val myCampsiteIds = myCampsites.map { it.campsiteId }.toSet()


                reservationRepository.observeReservations().collect { allReservations ->

                    // Each mode decides which reservations to show
                    val reservations = when (user.userType) {
                        UserType.Entrant -> allReservations.filter { it.userId == user.userId }
                        UserType.CampsiteOwner -> allReservations.filter { it.userId == user.userId ||
                                it.campsiteId in myCampsiteIds }
                        UserType.Admin -> allReservations.filter { it.userId == user.userId ||
                                it.campsiteId in myCampsiteIds }

                    }.sortedByDescending { it.timestamp }

                    val today = DayFormatter.fromMillis(System.currentTimeMillis())

                    val items = reservations.map { reservation ->
                        val isAtMyCamp = reservation.campsiteId in myCampsiteIds
                        reservation.toItem(
                            campsiteName = campsiteRepository.getCampsite(reservation.campsiteId)?.name,
                            canMakeReservation = reservation.userId == user.userId &&
                                    reservation.reservationState == ReservationState.Pending,
                            isMine = reservation.userId == user.userId,
                            isAtMyCamp = isAtMyCamp,
                            guestName = if (isAtMyCamp) guestName(reservation.userId) else "",
                            today = today,
                        )
                    }


                    _state.value = ReservationsState.Content(
                        items = items,
                        myCampsites = myCampsites.map {
                            CampsiteOption(it.campsiteId, it.name.ifBlank { "Campsite" })
                        },
                    )
                }
            } catch (e: Exception) {
                _state.value = ReservationsState.Error("Unable to load reservations")
            }
        }
    }

    /**
     * Turning a reservation into a card
     *
     * @param campsiteName Name of campsite
     * @param canMakeReservation boolean
     * @param isMine True when the signed-in user made it
     * @param isAtMyCamp True when it's at a campsite the user owns
     * @param guestName Who made it, blank when not shown to an owner
     * @param today Today as days
     * @return The card for this reservation
     */
    private fun Reservation.toItem(
        campsiteName: String?,
        canMakeReservation: Boolean,
        isMine: Boolean,
        isAtMyCamp: Boolean,
        guestName: String,
        today: Int,
    ): ReservationItem {

        val checkIn = DayFormatter.fromDateString(datesBooked.startDate)
        val checkOut = DayFormatter.fromDateString(datesBooked.endDate)

        return ReservationItem(
            reservationId = reservationId,
            state = reservationState,
            title = campsiteName?.takeIf { it.isNotBlank() }?: "Campsite",
            dateText = daysAgoText(timestamp),
            canMakeReservation = canMakeReservation,
            campsiteId = campsiteId,
            guestName = guestName,
            stayText = stayText(checkIn, checkOut),
            stayLabel = stayLabel(checkIn, checkOut, today),
            checkInDay = checkIn ?: Int.MAX_VALUE,
            hasEnded = checkOut != null && checkOut < today,
            isMine = isMine,
            isAtMyCamp = isAtMyCamp,
        )
    }

    /**
     * Switches tabs
     *
     * @param tab The tab the user clicked
     */
    fun selectTab(tab: ReservationTab) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedTab = tab) ?: it }
    }

    /**
     * Switches between My Reservations and Reservations At My Camps
     *
     * @param scope The scope the user clicked
     */
    fun selectScope(scope: ReservationToDisplay) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedScope = scope) ?: it }
    }

    /**
     * Filters Reservations At My Camps to one campsite
     *
     * @param campsiteId The campsite picked, or null for all campsites
     */
    fun selectCampsite(campsiteId: String?) {
        _state.update { (it as? ReservationsState.Content)?.copy(selectedCampsiteId = campsiteId) ?: it }
    }

    /**
     * Responsible for the name shown to owners for whoever made a reservation
     *
     * @param userId The guest's user ID
     * @return "First Last", or "Guest" if they have no profile or name
     */
    private suspend fun guestName(userId: String): String {
        val profile = profileRepository.getProfile(userId) ?: return "Guest"
        val name = "${profile.firstName} ${profile.lastName}".trim()
        return name.ifBlank { "Guest" }
    }

    /**
     * Responsible for whether a stay is still ahead or happening now
     *
     * @param checkIn First day
     * @param checkOut Last day
     * @param today Today as days
     * @return "Upcoming", "In progress", or "" when it has ended or has no dates
     */
    private fun stayLabel(checkIn: Int?, checkOut: Int?, today: Int): String {
        if (checkIn == null || checkOut == null) return ""
        return when {
            today < checkIn -> "Upcoming"
            today <= checkOut -> "In progress"
            else -> ""
        }
    }

    /**
     *  Responsible for showing a stay's dates
     *
     * @param checkIn First day, as days since 1970, or null
     * @param checkOut Last day, as days since 1970, or null
     * @return e.g. "Dec 1 - Dec 5, 2026", or "" when there are no dates
     */
    private fun stayText(checkIn: Int?, checkOut: Int?): String {
        return when {
            checkIn != null && checkOut != null ->
                "${DayFormatter.shortDate(checkIn)} - ${DayFormatter.shortDate(checkOut)}"
            checkIn != null -> DayFormatter.shortDate(checkIn)
            else -> ""
        }
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