package com.example.timewarp.feature.example

import androidx.lifecycle.ViewModel
import com.example.timewarp.domain.model.ExampleModel
import com.example.timewarp.domain.repository.ExampleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ExampleState(
    val example: String = "",
)

@HiltViewModel
class ExampleViewModel @Inject constructor (
    private val exampleRepository: ExampleRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleState())
    val uiState = _uiState.asStateFlow()

    fun doExample(): ExampleModel {
        return exampleRepository.example()
    }
}