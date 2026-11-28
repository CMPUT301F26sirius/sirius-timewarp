package com.example.timewarp.feature.notifications

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeNotificationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Notification
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.NotificationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import okhttp3.internal.wait
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NotificationsTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var notificationRepository: NotificationRepository

    private val ownerId = "owner-id"

    private val entrantId = "entrant-id"

    private val adminId = "admin-id"

    private val notificationId = "notification-id"

    private val notificationContent = "This is a test notification"

    private val incomingTab = "notification_tab_Incoming"
    private val outgoingTab = "notification_tab_Outgoing"
    private val allTab = "notification_tab_AllNotifications"

    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).addUser(
            User(userId = entrantId, userType = UserType.Entrant )
        )
        (userRepository as FakeUserRepository).addUser(
            User(userId = ownerId, userType = UserType.CampsiteOwner )
        )
        (userRepository as FakeUserRepository).addUser(
            User(userId = adminId, userType = UserType.Admin )
        )

        val notificationRepository = notificationRepository as FakeNotificationRepository
        notificationRepository.addNotification(
            Notification(
                notificationId = notificationId,
                notification = notificationContent,
                fromUserId = ownerId,
                toUserId = entrantId,
            )
        )
        notificationRepository.addNotification(
            Notification(
                notificationId = "other-id",
                notification = "other content",
                fromUserId = "other-owner",
                toUserId = "other-entrant",
            )
        )
    }

    @Test
    fun checkEntrantTabs() {
        (authRepository as FakeAuthRepository).setCurrentUserId(entrantId)
        openNotificationsScreen()
        composeRule.onNodeWithTag(incomingTab).assertIsNotDisplayed()
        composeRule.onNodeWithTag(outgoingTab).assertIsNotDisplayed()
        composeRule.onNodeWithTag(allTab).assertIsNotDisplayed()
    }

    @Test
    fun checkOwnerTabs() {
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        openNotificationsScreen()
        waitForTag(incomingTab)
        composeRule.onNodeWithTag(incomingTab).assertIsDisplayed()
        composeRule.onNodeWithTag(outgoingTab).assertIsDisplayed()
        composeRule.onNodeWithTag(allTab).assertIsNotDisplayed()
    }

    @Test
    fun checkAdminTabs() {
        (authRepository as FakeAuthRepository).setCurrentUserId(adminId)
        openNotificationsScreen()
        waitForTag("notification_tab_Incoming")
        composeRule.onNodeWithTag(incomingTab).assertIsDisplayed()
        composeRule.onNodeWithTag(outgoingTab).assertIsDisplayed()
        composeRule.onNodeWithTag(allTab).assertIsDisplayed()
    }

    @Test fun checkEntrantNotifications() {
        (authRepository as FakeAuthRepository).setCurrentUserId(entrantId)
        openNotificationsScreen()
        waitForText(notificationContent)
        composeRule.onNodeWithText("other content").assertDoesNotExist()
    }

    @Test fun checkOwnerNotifications() {
        (authRepository as FakeAuthRepository).setCurrentUserId(ownerId)
        openNotificationsScreen()
        waitForTag(incomingTab)
        composeRule.onNodeWithText(notificationContent).assertDoesNotExist()
        composeRule.onNodeWithTag(outgoingTab).performClick()
        waitForText(notificationContent)
        composeRule.onNodeWithText("other content").assertDoesNotExist()
    }

    @Test fun checkAdminNotifications() {
        (authRepository as FakeAuthRepository).setCurrentUserId(adminId)
        openNotificationsScreen()
        waitForTag(incomingTab)
        composeRule.onNodeWithText(notificationContent).assertDoesNotExist()
        composeRule.onNodeWithTag(incomingTab).performClick()
        composeRule.onNodeWithText(notificationContent).assertDoesNotExist()
        composeRule.onNodeWithTag(allTab).performClick()
        waitForText(notificationContent)
        waitForText("other content")
    }
    /*
    ====================================================================================
     */

    private fun openNotificationsScreen() {
        scenario = ActivityScenario.launch(MainActivity::class.java)
        waitForText("Notifications")
        composeRule.onNodeWithText("Notifications").performClick()
        waitForText("PITCH/NOTIFICATIONS")
    }

    private fun waitForText(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }

    private fun waitForTag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    @After
    fun tearDown() {
        if(::scenario.isInitialized) scenario.close()
    }
}