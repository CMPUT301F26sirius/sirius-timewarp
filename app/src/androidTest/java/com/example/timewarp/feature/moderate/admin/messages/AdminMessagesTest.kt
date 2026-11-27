package com.example.timewarp.feature.moderate.admin.messages

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

private const val CURRENT_USER_ID = "messages-current"
private const val ALEX_ID = "messages-alex"
private const val BRYN_ID = "messages-bryn"
private const val UNRELATED_USER_ID = "messages-unrelated"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AdminMessagesTest {
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
            addUser(User(userId = CURRENT_USER_ID, userType = UserType.Admin))
            addUser(User(userId = ALEX_ID, userType = UserType.Entrant))
            addUser(User(userId = BRYN_ID, userType = UserType.Entrant))
            addUser(User(userId = UNRELATED_USER_ID, userType = UserType.Entrant))
        }
        (profileRepository as FakeProfileRepository).apply {
            clear()
            addProfile(Profile(userId = ALEX_ID, firstName = "Alex", lastName = "Parker"))
            addProfile(Profile(userId = BRYN_ID, firstName = "Bryn", lastName = "Taylor"))
            addProfile(Profile(userId = UNRELATED_USER_ID, firstName = "Stranger", lastName = "Person"))
        }
        (messageRepository as FakeMessageRepository).apply {
            clear()
            addMessage(
                Message(
                    timestamp = 100,
                    message = "Old Alex message",
                    fromUserId = ALEX_ID,
                    toUserId = CURRENT_USER_ID,
                )
            )
            addMessage(
                Message(
                    timestamp = 300,
                    message = "Latest Alex message",
                    fromUserId = CURRENT_USER_ID,
                    toUserId = ALEX_ID,
                )
            )
            addMessage(
                Message(
                    timestamp = 200,
                    message = "Latest Bryn message",
                    fromUserId = BRYN_ID,
                    toUserId = CURRENT_USER_ID,
                )
            )
            addMessage(
                Message(
                    timestamp = 400,
                    message = "Unrelated private message",
                    fromUserId = UNRELATED_USER_ID,
                    toUserId = "someone-else",
                )
            )
        }
    }

    private fun waitForTag(tag: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForText(text: String) {
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText(text, substring = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    @Test
    fun showsAllMessagesWithSenderAndRecipient() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            waitForText("Log")
            composeRule.onNodeWithText("Log").performClick()
            waitForTag("admin_tab_Profiles")
            composeRule.onNodeWithTag("admin_tab_Messages").performClick()
            waitForTag("admin_message_item_3")

            (0..3).forEach { index ->
                composeRule.onNodeWithTag("admin_message_item_$index").assertIsDisplayed()
            }
            listOf(
                "Old Alex message",
                "Latest Alex message",
                "Latest Bryn message",
                "Unrelated private message",
                "From: Alex Parker",
                "From: messages-current",
                "From: Bryn Taylor",
                "From: Stranger Person",
                "To: Alex Parker",
                "To: someone-else"
            ).forEach { text ->
                composeRule.onNodeWithText(text).assertIsDisplayed()
            }
            composeRule.onAllNodesWithText("To: messages-current").assertCountEquals(2)
        } finally {
            scenario.close()
        }
    }
}
