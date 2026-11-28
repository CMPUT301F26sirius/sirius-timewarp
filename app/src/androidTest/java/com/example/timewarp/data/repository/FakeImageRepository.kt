package com.example.timewarp.data.repository

import android.net.Uri
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ImageRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.emptyList

/**
 * Fake implementation of [ImageRepository].
 *
 * Stores image metadata in memory and retains each selected URI as the image URL
 * so [com.example.timewarp.ui.component.RemoteImage] can load it through Coil.
 */
@Singleton
class FakeImageRepository @Inject constructor(
    private val authRepository: AuthRepository
) : ImageRepository {
    private val images = ConcurrentHashMap<String, Image>()
    private val imagesFlow = MutableStateFlow<List<Image>>(emptyList())

    private fun publish() {
        imagesFlow.value = images.values.toList()
    }

    override suspend fun getImage(imageId: String): Image? = images[imageId]

    override suspend fun uploadImage(uri: Uri): Image {
        val userId = authRepository.getUserId()
            ?: throw IllegalStateException("User must be authenticated.")
        val imageId = UUID.randomUUID().toString()
        val image = Image(
            uploadingUserId = userId,
            imageId = imageId,
            url = uri.toString(),
            storagePath = "fake/images/$imageId"
        )
        images[imageId] = image
        return image
    }

    override suspend fun deleteImage(imageId: String): Boolean {
        val removed = images.remove(imageId) != null
        publish()
        return removed
    }

    fun clear() {
        images.clear()
        publish()
    }

    override fun observeImages(): Flow<List<Image>> = imagesFlow

    fun addImage(image: Image) {
        images[image.imageId] = image
        publish()
    }

}
