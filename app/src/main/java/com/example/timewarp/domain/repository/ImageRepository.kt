package com.example.timewarp.domain.repository

import android.net.Uri
import com.example.timewarp.domain.model.Image

/**
 * [ImageRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface ImageRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getImage(imageId: String): Image?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun uploadImage(uri: Uri): Image?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun deleteImage(imageId: String): Boolean
}