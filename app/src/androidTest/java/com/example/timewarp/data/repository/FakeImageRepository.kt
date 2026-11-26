package com.example.timewarp.data.repository

import android.net.Uri
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ImageRepository
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

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

    override suspend fun deleteImage(imageId: String): Boolean =
        images.remove(imageId) != null

    fun clear() {
        images.clear()
    }
}
