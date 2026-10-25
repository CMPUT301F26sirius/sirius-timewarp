package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.ExampleModel

interface ExampleRepository {
    fun example(): ExampleModel
}