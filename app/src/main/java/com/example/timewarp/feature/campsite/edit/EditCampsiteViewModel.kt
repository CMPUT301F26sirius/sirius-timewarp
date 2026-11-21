package com.example.timewarp.feature.campsite.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.feature.campsite.create.CreateCampsiteState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EditCampsiteState {
    data object Loading : EditCampsiteState
    data class Error(val message: String) : EditCampsiteState
    data class Ready(
        val campsiteId: String,
        val draft: CreateCampsiteState
    ) : EditCampsiteState
}



/**
 * Loads an existing campsite into an editable draft and saves changes after
 * validating the form and verifying that the signed-in user owns the campsite.
 *
 * @param campsiteRepository Reads and updates the existing campsite record.
 * @param authRepository Provides the signed-in user ID for ownership checks.
 */
@HiltViewModel
class EditCampsiteViewModel @Inject constructor(
    private val campsiteRepository: CampsiteRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow<EditCampsiteState>(EditCampsiteState.Loading)
    val state = _state.asStateFlow()

    private var requestedId: String? = null
    private var loadJob: Job? = null

    /**
     * Loads the campsite and prefills the edit form.
     * A failed load can be retried by calling this again with the same ID.
     *
     * @param campsiteId ID of the campsite to load.
     */

    fun observe(campsiteId: String) {
        if (requestedId == campsiteId && _state.value !is EditCampsiteState.Error) return

        loadJob?.cancel()
        requestedId = campsiteId
        _state.value = EditCampsiteState.Loading

        loadJob = viewModelScope.launch {
            try {
                val campsite = campsiteRepository.getCampsite(campsiteId)
                val userId = authRepository.getUserId()

                _state.value = when {
                    campsite == null ->
                        EditCampsiteState.Error("Campsite not found.")

                    userId.isNullOrBlank() || userId != campsite.ownerUserId ->
                        EditCampsiteState.Error("Only the owner can edit this campsite.")

                    else -> EditCampsiteState.Ready(
                        campsiteId = campsiteId,
                        draft = CreateCampsiteState(
                            name = campsite.name,
                            description = campsite.description,
                            address = campsite.location.address,
                            maxGuests = campsite.occupancy.maxGuests.toString(),
                            costPerNight = campsite.pricing.costPerNight.toString(),
                            preparationFee = campsite.pricing.preparationFee.toString(),
                            minimumStayNights = campsite.minimumStayNights.toString(),
                            amenities = campsite.amenities,
                            tags = campsite.tags,
                            rules = campsite.rules.joinToString("\n"),
                            cancellationPolicy = campsite.cancellationPolicy.policy,
                            startDate = campsite.availability.startDate,
                            endDate = campsite.availability.endDate,
                            daysOfWeek = campsite.availability.daysOfWeek,
                            checkInTime = campsite.checkInTime.ifBlank { "14:00" },
                            checkOutTime = campsite.checkOutTime.ifBlank { "11:00" }
                        )
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = EditCampsiteState.Error("Could not load this campsite.")
            }
        }
    }

    /**
     * Updates the current form values and clears the previous save error.
     *
     * @param draft The latest form values from the screen.
     */

    fun updateDraft(draft: CreateCampsiteState) {
        val current = _state.value as? EditCampsiteState.Ready ?: return
        if (current.draft.isSaving || current.draft.savedCampsiteId != null) return

        _state.value = current.copy(draft = draft.copy(saveError = null))
    }
    /**
     * Validates the draft, checks ownership, and updates the existing campsite.
     * If the save fails, keeps the draft available for retry.
     */
    fun save() {
        val current = _state.value as? EditCampsiteState.Ready ?: return
        val draft = current.draft

        if (draft.isSaving || draft.savedCampsiteId != null) return
        if (draft.fieldErrors.isNotEmpty()) {
            _state.value = current.copy(draft = draft.copy(showErrors = true))
            return
        }

        _state.value = current.copy(draft = draft.copy(isSaving = true, saveError = null))

        viewModelScope.launch {
            try {
                val campsite = campsiteRepository.getCampsite(current.campsiteId)
                val userId = authRepository.getUserId()

                if (campsite == null || userId.isNullOrBlank() ||
                    userId != campsite.ownerUserId
                ) {
                    _state.value = current.copy(
                        draft = draft.copy(saveError = "This campsite is no longer available to edit.")
                    )
                    return@launch
                }

                val updated = campsite.copy(
                    name = draft.name.trim(),
                    description = draft.description.trim(),
                    location = campsite.location.copy(address = draft.address.trim()),
                    occupancy = campsite.occupancy.copy(maxGuests = draft.maxGuests.toInt()),
                    pricing = campsite.pricing.copy(
                        costPerNight = draft.costPerNight.toDouble(),
                        preparationFee = draft.preparationFee.toDouble()
                    ),
                    amenities = draft.amenities.distinct(),
                    tags = draft.tags.distinct(),
                    rules = draft.rules.lines().map { it.trim() }.filter { it.isNotEmpty() },
                    cancellationPolicy = campsite.cancellationPolicy.copy(
                        policy = draft.cancellationPolicy.trim()
                    ),
                    minimumStayNights = draft.minimumStayNights.toInt(),
                    checkInTime = draft.checkInTime,
                    checkOutTime = draft.checkOutTime,
                    availability = campsite.availability.copy(
                        startDate = draft.startDate,
                        endDate = draft.endDate,
                        daysOfWeek = draft.daysOfWeek
                    )
                )

                campsiteRepository.editCampsite(current.campsiteId, updated)
                _state.value = current.copy(
                    draft = draft.copy(saveError = null, savedCampsiteId = current.campsiteId)
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                _state.value = current.copy(
                    draft = draft.copy(saveError = "Could not save your changes. Please try again.")
                )
            }
        }
    }
}