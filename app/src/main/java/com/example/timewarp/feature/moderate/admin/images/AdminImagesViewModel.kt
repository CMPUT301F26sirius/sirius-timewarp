package com.example.timewarp.feature.moderate.admin.images

import android.adservices.adid.AdId
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
 */
@HiltViewModel
class AdminImagesViewModel @Inject constructor(
    private val imageRepository: ImageRepository,
) : ViewModel() {
    private val _state = MutableStateFlow<AdminImages>(AdminImages.Loading)
    val state: StateFlow<AdminImages> = _state.asStateFlow()

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
}