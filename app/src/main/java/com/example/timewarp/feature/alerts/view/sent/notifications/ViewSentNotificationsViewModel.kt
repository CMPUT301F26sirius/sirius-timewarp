package com.example.timewarp.feature.alerts.view.sent.notifications

import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.usecase.ObserveCurrentUserUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ViewSentNotificationsViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val observeCurrentUser: ObserveCurrentUserUseCase
) {
    
}