package com.example.timewarp.feature.campsite.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Availability
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.CancellationPolicy
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Pricing
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import com.example.timewarp.domain.usecase.ObserveUserProfileUseCase

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.PrivateKey
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

/**
 * [CreateCampsiteState] State of the ui
 */


// All values entered on the registration form.
data class CreateCampsiteState(
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val maxGuests: String = "",
    val costPerNight: String = "",
    val amenities: List<String> = emptyList(),
    val rules: String = "",
    val cancellationPolicy: String = "",
    val minimumStayNights: String = "1",
    val preparationFee: String = "0",
    val tags: List<String> = emptyList(),
    val startDate: String = "",
    val endDate: String = "",
    val daysOfWeek: List<Int> = (1..7).toList(),
    val checkInTime: String = "14:00",
    val checkOutTime: String = "11:00",
    val isSaving: Boolean = false,
    val showErrors: Boolean = false,
    val saveError: String? = null,
    val savedCampsiteId: String? = null
) {
    val fieldErrors: Map<String, String>
        get() = buildMap {
            if (name.isBlank()) put("name", "Enter a campsite name.")
            if (description.isBlank()) put("description", "Describe your campsite.")
            if (address.isBlank()) put("address", "Enter the campsite address.")
            if ((maxGuests.toIntOrNull() ?: 0) < 1) {
                put("maxGuests", "Enter a whole number of guests greater than zero.")
            }
            val price = costPerNight.toDoubleOrNull()
            if (price == null || !price.isFinite() || price < 0) {
                put("costPerNight", "Enter a valid nightly price of zero or more.")
            }
            val fee = preparationFee.toDoubleOrNull()
            if (fee == null || !fee.isFinite() || fee < 0) put("preparationFee", "Enter a valid fee of zero or more.")
            if ((minimumStayNights.toIntOrNull() ?: 0) < 1) put("minimumStayNights", "Minimum stay must be at least one night.")
            val timeFormat = Regex("([01][0-9]|2[0-3]):[0-5][0-9]")
            if (!timeFormat.matches(checkInTime)) put("checkInTime", "Use 24-hour time, for example 14:00.")
            if (!timeFormat.matches(checkOutTime)) put("checkOutTime", "Use 24-hour time, for example 11:00.")
            if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
                val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
                val start = runCatching { format.parse(startDate) }.getOrNull()
                val end = runCatching { format.parse(endDate) }.getOrNull()
                val dateFormat = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")
                if (!dateFormat.matches(startDate) || !dateFormat.matches(endDate) ||
                    start == null || end == null || end.before(start)) {
                    put("availability", "Use YYYY-MM-DD dates, with the end on or after the start.")
                }
                if (daysOfWeek.isEmpty()) put("availability", "Select at least one available day.")
            }
        }
}



/**
 * [CreateCampsiteViewModel] for the [CreateCampsiteScreen].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CreateCampsiteViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val ratingRepository: RatingRepository,
    private val lotteryRepository: LotteryRepository,
    private val authRepository: AuthRepository,
    private val observeUserProfile: ObserveUserProfileUseCase,

    private val observeCurrentUser: ObserveCurrentUserUseCase,

    private val userRepository: UserRepository,

) : ViewModel() {


    private val _state = MutableStateFlow(CreateCampsiteState())

    val state: StateFlow<CreateCampsiteState> =
        _state.asStateFlow()




    // Reuse the document ID on retries, so an uncertain network response cannot create duplicates.
    private val campsiteId = UUID.randomUUID().toString()

    // Update the form only while the user is still editing it.
    fun updateDraft(newState: CreateCampsiteState) {
        if (_state.value.isSaving || _state.value.savedCampsiteId != null) return
        _state.value = newState.copy(saveError = null)


    }

    fun register() {
        val draft = _state.value
        if (draft.isSaving || draft.savedCampsiteId != null) return
        if (draft.fieldErrors.isNotEmpty()) {
            _state.update { it.copy(showErrors = true) }
            return
        }
        val userId = authRepository.getUserId()
        if (userId.isNullOrBlank()) {
            _state.update { it.copy(saveError = "Please sign in before registering a campsite.") }
            return
        }

        _state.update { it.copy(isSaving = true, saveError = null) }
        viewModelScope.launch {
            try {
                // Check the role again when saving, even if this route was opened directly.
                val user = userRepository.getUser(userId)
                if (user?.userType != UserType.CampsiteOwner) {
                    _state.update {
                        it.copy(isSaving = false, saveError = "Only campsite owners can register a campsite.")
                    }
                    return@launch
                }
                campsiteRepository.createCampsite(
                    Campsite(
                        campsiteId = campsiteId,
                        ownerUserId = userId,
                        name = draft.name.trim(),
                        description = draft.description.trim(),
                        location = Location(address = draft.address.trim()),
                        occupancy = Occupancy(maxGuests = draft.maxGuests.toInt()),
                        pricing = Pricing(
                            costPerNight = draft.costPerNight.toDouble(),
                            preparationFee = draft.preparationFee.toDouble()
                        ),
                        amenities = draft.amenities.distinct(),
                        rules = draft.rules.lines().map { it.trim() }.filter { it.isNotEmpty() },
                        cancellationPolicy = CancellationPolicy(draft.cancellationPolicy.trim()),
                        tags = draft.tags,
                        minimumStayNights = draft.minimumStayNights.toInt(),
                        checkInTime = draft.checkInTime,
                        checkOutTime = draft.checkOutTime,
                        availability = Availability(
                            startDate = draft.startDate,
                            endDate = draft.endDate,
                            daysOfWeek = draft.daysOfWeek
                        )
                    )
                )
                _state.update { it.copy(isSaving = false, savedCampsiteId = campsiteId) }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.update {
                    it.copy(isSaving = false, saveError = "Could not register your campsite. Please try again.")
                }
            }
        }
    }

}