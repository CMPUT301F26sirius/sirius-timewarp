package com.example.timewarp.feature.campsite.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.Campsite
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

import kotlin.uuid.ExperimentalUuidApi


@OptIn(ExperimentalUuidApi::class)
@HiltViewModel
class CampsiteDetailViewModel @Inject constructor() : ViewModel() {

    //Temporary for testing
    val campsite = Campsite(
        id = "123",
        ownerId = "456",
        name = "Lakeview Campsite",
        description = "Beautiful campsite near a lake"
    )

    var campsiteId by mutableStateOf("")
        private set

    fun selectCampsite(id: String) {
        campsiteId = id
    }
}