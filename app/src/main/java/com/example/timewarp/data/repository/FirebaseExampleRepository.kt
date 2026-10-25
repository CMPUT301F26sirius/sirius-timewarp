package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.ExampleModel
import com.example.timewarp.domain.repository.ExampleRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseExampleRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : ExampleRepository {

    override fun example(): ExampleModel {
        return ExampleModel()
    }
}