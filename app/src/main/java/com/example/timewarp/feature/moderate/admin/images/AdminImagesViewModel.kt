package com.example.timewarp.feature.moderate.admin.images

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.repository.ImageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This interface is for UI state for the admin images tab
 */
sealed interface AdminImages {
    data object Loading : AdminImages

    data class Content(val images: List<Image>) : AdminImages

    data class Error(val message: String): AdminImages
}

/**
 * This class streams every uploaded image for the admin images tab
 *
 * @property imageRepository used to observe and delete images
 */
@HiltViewModel
class AdminImagesViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
) : ViewModel() {
    // State
    private val _state = MutableStateFlow<AdminImages>(AdminImages.Loading)
    val state: StateFlow<AdminImages> = _state.asStateFlow()

    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    init {
        viewModelScope.launch {
            imageRepository.observeImages().catch {
                _state.value = AdminImages.Error("Unable to load images")
            }
                .collect {
                    _state.value = AdminImages.Content(it)
                }
        }
    }

    /**
     * This function will delete the selected image from the list.
     *
     * @param imageId
     */
    fun deleteImages(imageId: String) {
        viewModelScope.launch {
            try {
                imageRepository.deleteImage(imageId)
            } catch (e: Exception) {
                _state.value = AdminImages.Error("Unable to delete the image")
            }
        }
    }

    /**
     * This function lets you add or remove an image from the selection
     */
    fun toggleSelected(imageId: String) {
        _selected.value = if (imageId in _selected.value) {
            _selected.value - imageId
        } else {
            _selected.value + imageId
        }
    }

    /**
     * This function lets you select every image or clear the selection
     */
    fun toggleSelectAll(allIds: List<String>) {
        _selected.value = if (
            allIds.isNotEmpty() && _selected.value.containsAll(allIds)
        ) {
            emptySet()
        } else {
            allIds.toSet()
        }

    }

    /**
     * This function lets delete every selected image
     */
    fun deleteSelected() {
        val ids = _selected.value
        viewModelScope.launch {
            try {
                ids.forEach { imageRepository.deleteImage(it) }
            } catch (e: Exception) {
                _state.value = AdminImages.Error("Unable to delete the image")
            }
            _selected.value = emptySet()
        }
    }

}