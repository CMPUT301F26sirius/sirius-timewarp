package com.example.timewarp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class TimewarpApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}