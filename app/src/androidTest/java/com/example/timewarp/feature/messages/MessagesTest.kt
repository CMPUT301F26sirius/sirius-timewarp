package com.example.timewarp.feature.messages

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
import org.junit.Assert.assertTrue
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
class MessagesTest {
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

    private fun openMessages() {
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithText("Messages").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Messages").performClick()
        composeRule.waitUntil(10_000) {
            composeRule.onAllNodesWithTag("message_conversation_$ALEX_ID")
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test
    fun inboxShowsLatestMessageForEachCorrespondentOnly() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            openMessages()

            composeRule.onNodeWithText("Alex Parker", substring = true).assertIsDisplayed()
            composeRule.onNodeWithText("Latest Alex message", substring = true).assertIsDisplayed()
            composeRule.onNodeWithText("Bryn Taylor", substring = true).assertIsDisplayed()
            composeRule.onNodeWithText("Latest Bryn message", substring = true).assertIsDisplayed()
            composeRule.onNodeWithText("Old Alex message", substring = true).assertDoesNotExist()
            composeRule.onNodeWithText("Stranger Person", substring = true).assertDoesNotExist()
            composeRule.onNodeWithText("Unrelated private message", substring = true).assertDoesNotExist()
        } finally {
            scenario.close()
        }
    }

    @Test
    fun inboxOrdersLatestCorrespondenceFirst() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            openMessages()

            val alexTop = composeRule
                .onNodeWithTag("message_conversation_$ALEX_ID")
                .fetchSemanticsNode()
                .boundsInRoot.top
            val brynTop = composeRule
                .onNodeWithTag("message_conversation_$BRYN_ID")
                .fetchSemanticsNode()
                .boundsInRoot.top

            assertTrue("Most recent conversation should appear first", alexTop < brynTop)
        } finally {
            scenario.close()
        }
    }
}
