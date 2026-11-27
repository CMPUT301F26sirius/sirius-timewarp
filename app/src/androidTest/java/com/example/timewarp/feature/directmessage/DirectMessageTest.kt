package com.example.timewarp.feature.directmessage

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeMessageRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Message
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.MessageRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

private const val CURRENT_USER_ID = "direct-message-current"
private const val TARGET_USER_ID = "direct-message-target"
private const val OTHER_USER_ID = "direct-message-other"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DirectMessageTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var messageRepository: MessageRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        (authRepository as FakeAuthRepository).setCurrentUserId(CURRENT_USER_ID)
        (userRepository as FakeUserRepository).apply {
            clear()
            addUser(User(userId = CURRENT_USER_ID, userType = UserType.Entrant))
            addUser(User(userId = TARGET_USER_ID, userType = UserType.Entrant))
            addUser(User(userId = OTHER_USER_ID, userType = UserType.Entrant))
        }
        (profileRepository as FakeProfileRepository).apply {
            clear()
            addProfile(Profile(userId = TARGET_USER_ID, firstName = "Jordan", lastName = "Lee"))
        }
        (messageRepository as FakeMessageRepository).clear()
    }

    private fun openTargetConversation() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Messages").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Messages").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Jordan Lee", substring = true)
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Jordan Lee", substring = true).performClick()
    }

    @Test
    fun conversationShowsOnlyMessagesBetweenCurrentUserAndTarget() {
        (messageRepository as FakeMessageRepository).apply {
            addMessage(
                Message(
                    timestamp = 10,
                    message = "A message from Jordan",
                    fromUserId = TARGET_USER_ID,
                    toUserId = CURRENT_USER_ID,
                )
            )
            addMessage(
                Message(
                    timestamp = 20,
                    message = "A reply to Jordan",
                    fromUserId = CURRENT_USER_ID,
                    toUserId = TARGET_USER_ID,
                )
            )
            addMessage(
                Message(
                    timestamp = 30,
                    message = "A message from someone else",
                    fromUserId = OTHER_USER_ID,
                    toUserId = CURRENT_USER_ID,
                )
            )
        }

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            openTargetConversation()

            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithText("A message from Jordan")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("Jordan Lee").assertIsDisplayed()
            composeRule.onNodeWithText("A message from Jordan").assertIsDisplayed()
            composeRule.onNodeWithText("A reply to Jordan").assertIsDisplayed()
            composeRule.onNodeWithText("A message from someone else").assertDoesNotExist()
        } finally {
            scenario.close()
        }
    }

    @Test
    fun sendingMessageAddsItToTheConversation() {
        (messageRepository as FakeMessageRepository).addMessage(
            Message(
                timestamp = 10,
                message = "Starting message",
                fromUserId = TARGET_USER_ID,
                toUserId = CURRENT_USER_ID,
            )
        )

        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            openTargetConversation()
            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithText("Starting message")
                    .fetchSemanticsNodes().isNotEmpty()
            }

            composeRule.onNodeWithText("Write a message")
                .performTextInput("A newly sent message")
            composeRule.onNodeWithText("Send").performClick()

            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithText("A newly sent message")
                    .fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithText("A newly sent message").assertIsDisplayed()
        } finally {
            scenario.close()
        }
    }
}
