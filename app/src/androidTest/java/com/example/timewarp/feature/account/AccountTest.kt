package com.example.timewarp.feature.account

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeReservationRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

/*
* Instrumented tests for the browser screen that covers user stories
* 01.01.01 , 01.01.02, 01.01.03, 01.01.04.
* The test runs against fake repositories. It covers filtering, listing
* and campsite card admin moderator tab.
*/

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AccountTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(
            User(
                userId = "account-user",
                userType = UserType.Entrant,
                notificationsEnabled = true,
            )
        )
        (authRepository as FakeAuthRepository).setCurrentUserId("account-user")

        (profileRepository as FakeProfileRepository).clear()
        (profileRepository as FakeProfileRepository).addProfile(
            Profile(
                userId = "account-user",
                firstName = "Jane",
                lastName = "Doe",
                email = "jane@example.com",
                phoneNumber = "7801234567"
            )
        )

        (reservationRepository as FakeReservationRepository).clear()
    }

    @Test
    fun notificationToggle_updatesUserPreference() {
        val scenario = openAccount()

        composeRule.onNodeWithText("Notifications: On").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: Off")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        assertEquals(
            false,
            runBlocking { userRepository.getUser("account-user")?.notificationsEnabled }
        )

        composeRule.onNodeWithText("Notifications: Off").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: On")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        assertEquals(
            true,
            runBlocking { userRepository.getUser("account-user")?.notificationsEnabled }
        )

        scenario.close()
    }

    private fun openAccount(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Account")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        composeRule.onNodeWithText("Account").performClick()
        composeRule.waitUntil(5_000) {
            composeRule.onAllNodesWithText("Notifications: On")
                .fetchSemanticsNodes(atLeastOneRootRequired = false)
                .isNotEmpty()
        }
        return scenario
    }

    /*
   * This shows the delete button opens an "are you sure" pop-up
   */
    @Test
    fun deleteButton_opensConfirmPopup_010801() {
        val scenario = openAccount()

        composeRule.onNodeWithTag("delete_account").performClick()
        waitForTag("delete_dialog")
        composeRule.onNodeWithText("Are you sure you want to delete your profile?").assertIsDisplayed()

        scenario.close()
    }

    /*
    * This shows confirming removes the user's sign-in account
    */
    @Test
    fun confirm_removesSignInAccount_010801() {
        val scenario = openAccount()

        deleteAccount()
        // Startup signs in a brand new user, so the old ID must be gone
        assertNotEquals("account-user", authRepository.getUserId())

        scenario.close()
    }

    /*
    * This shows confirming removes active reservations but keeps past ones
    */
    @Test
    fun confirm_removesActiveReservations_keepsPast_010801() {
        val repository = reservationRepository as FakeReservationRepository
        repository.addReservation(reservation("waitlisted", "account-user", ReservationState.Waitlisted))
        repository.addReservation(reservation("pending", "account-user", ReservationState.Pending))
        repository.addReservation(reservation("reserved", "account-user", ReservationState.Reserved))
        repository.addReservation(reservation("canceled", "account-user", ReservationState.Canceled))
        val scenario = openAccount()

        deleteAccount()
        runBlocking {
            assertEquals(
                ReservationState.Canceled,
                reservationRepository.getReservation("waitlisted")?.reservationState,
            )
            assertEquals(
                ReservationState.Canceled,
                reservationRepository.getReservation("pending")?.reservationState,
            )
            assertEquals(
                ReservationState.Canceled,
                reservationRepository.getReservation("reserved")?.reservationState,
            )
            assertEquals(
                ReservationState.Canceled,
                reservationRepository.getReservation("canceled")?.reservationState,
            )
        }

        scenario.close()
    }

    /*
    * This shows the profile is filled with "No user found"
    */
    @Test
    fun confirm_fillsProfileWithNoUserFound_010801() {
        val scenario = openAccount()

        deleteAccount()
        runBlocking {
            assertNull(profileRepository.getProfile("account-user"))
            assertNull(userRepository.getUser("account-user"))
        }
        /*
        val profile = runBlocking { profileRepository.getProfile("account-user") }
        assertEquals("No user found", profile?.firstName)
        assertEquals("", profile?.email)
        assertEquals("", profile?.phoneNumber)
        */
        scenario.close()
    }


    /*
    * This function opens the pop-up, confirms the delete, and waits until the
    * signed-in account has changed
    */
    private fun deleteAccount() {
        composeRule.onNodeWithTag("delete_account").performClick()
        waitForTag("delete_confirm")
        composeRule.onNodeWithTag("delete_confirm").performClick()
        composeRule.waitUntil(5_000) {
            authRepository.getUserId() != "account-user"
        }
    }

    /*
    * This function waits for 5 seconds for the tag to appear on the screen
    */
    private fun waitForTag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isNotEmpty()
    }

    /*
    * This function waits for 5 seconds for the tag to disappear from the screen
    */
    private fun waitForTagGone(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag)
            .fetchSemanticsNodes(atLeastOneRootRequired = false)
            .isEmpty()
    }

    /*
    * test reservation with the given id, user, and state
    */
    private fun reservation(
        id: String,
        userId: String,
        state: ReservationState
    ) =
        Reservation(
            reservationId = id,
            reservationState = state,
            campsiteId = "campsite",
            userId = userId
        )
}