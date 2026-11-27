package com.example.timewarp.data.repository

import android.net.Uri
import com.example.timewarp.domain.model.Image
import com.example.timewarp.domain.repository.ImageRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [ImageRepository].
 *
 * Stores and retrieves images from the Firestore using cloud Storage
 *
 * @property firestore Firestore instance used to access images.
 */
@Singleton
class FirebaseImageRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth,
) : ImageRepository {
    private val imagesCollection = firestore.collection("images")

    /**
     * Retrieves a image by its id.
     *
     * @param imageId id of the image document to retrieve.
     * @return the image if the document exists, and `null` otherwise.
     */
    override suspend fun getImage(imageId: String): Image? {
        val document = imagesCollection.document(imageId).get().await()

        return if (document.exists()) {
            document.toObject(Image::class.java)
        } else {
            null
        }
    }

    /**
     * Uploads an image to Firebase Storage and stores its metadata
     * in Firestore.
     *
     * @param uri URI of the image to upload.
     * @return ID of the newly uploaded image.
     */
    override suspend fun uploadImage(uri: Uri): Image? {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("User must be authenticated.")

        val imageId = UUID.randomUUID().toString()
        val storagePath = "images/$imageId"

        val storageReference = storage.reference.child(storagePath)

        // Upload image bytes.
        storageReference.putFile(uri).await()

        try {
            // Retrieve download URL.
            val downloadUrl = storageReference.downloadUrl.await().toString()

            // Create Firestore metadata.
            val image = Image(
                uploadingUserId = userId,
                imageId = imageId,
                url = downloadUrl,
                storagePath = storagePath
            )

            // Add image to collection se we have metadata
            imagesCollection.document(imageId).set(image).await()

            // Return the image
            return image
        } catch (e: Exception) {
            return null
        }
    }

    /**
     * Deletes an image from Firebase Storage and Firestore.
     *
     * @param imageId ID of the image to delete.
     * @return Whether the image existed and was deleted.
     */
    override suspend fun deleteImage(imageId: String): Boolean {
        val image = getImage(imageId) ?: return false

        storage.reference.child(image.storagePath).delete().await()
        imagesCollection.document(imageId).delete().await()

        return true
    }
}
