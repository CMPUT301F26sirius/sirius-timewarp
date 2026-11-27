package com.example.timewarp.feature.moderate

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeProfileRepository
import com.example.timewarp.data.repository.FakeRatingRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Profile
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.Reservation
import com.example.timewarp.domain.model.ReservationState
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.ProfileRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.ReservationRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import junit.framework.TestCase
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AdminProfilesTest {
    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var profileRepository: ProfileRepository

    @Inject
    lateinit var ratingRepository: RatingRepository

    @Inject
    lateinit var reservationRepository: ReservationRepository

    /*
    * This function will sign in as admin and will seed an entrant with a profile and a
    * campsite owner with a profile and an entrant again but with no profile
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).clear()
        (profileRepository as FakeProfileRepository).clear()
        (ratingRepository as FakeRatingRepository).clear()
        (authRepository as FakeAuthRepository).setCurrentUserId("admin-user")

        val users = userRepository as FakeUserRepository
        users.addUser(User(userId = "admin-user", userType = UserType.Admin))
        users.addUser(User(userId = "entrant-1", userType = UserType.Entrant))
        users.addUser(User(userId = "owner-1", userType = UserType.CampsiteOwner))
        users.addUser(User(userId = "no-profile-1", userType = UserType.Entrant))

        val profiles = profileRepository as FakeProfileRepository
        profiles.addProfile(
            Profile(
                userId = "entrant-1",
                firstName = "Khan",
                lastName = "Entrant",
                email = "khan@gmail.com",
                phoneNumber = "587-599-2459"
            )
        )
        profiles.addProfile(
            Profile(
                userId = "owner-1",
                firstName = "Bob",
                lastName = "Owner",
                email = "bob@gmail.com",
            )
        )
    }
    /*
    * This function will wait 5 seconds for the tag to appear
    */
    private fun waitFor_Tag(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function will wait 5 seconds for the tag to disappear
    */
    private fun waitFor_TabGone(tag: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().isEmpty()
    }

    /*
    * This function will wait 5 seconds for the text to appear
    */
    private fun waitFor_Text(text: String) = composeRule.waitUntil(5_000) {
        composeRule.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
    }

    /*
    * This function opens the alerts then admin and then waits for the list of profiles to load
    */
    private fun openProfilesTab() {
        waitFor_Text("Log")
        composeRule.onNodeWithText("Log").performClick()
        waitFor_Tag("admin_profile_item_entrant-1")
    }

    /*
    * This function opens the profile of the user
    */
    private fun openProfileDialog(userId: String) {
        composeRule.onNodeWithTag("admin_profile_item_$userId").performClick()
        waitFor_Tag("admin_profile_detail")
    }

    /*
    * This function will click the delete account button and then clicks the confirm delete button
    */
    private fun delete_Account(){
        composeRule.onNodeWithText("Delete Account", ignoreCase = true).performClick()
        if (composeRule.onAllNodesWithTag("confirm_admin_action")
            .fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("confirm_admin_action").performClick()
        }
    }

    /*
    * This test will show every user in the list including with no profile
    */
    @Test
    fun admin_profileTab_ShowsAllUsers() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openProfilesTab()
        listOf("admin-user", "entrant-1", "owner-1", "no-profile-1").forEach {
            composeRule.onNodeWithTag("admin_profile_item_$it").assertIsDisplayed()
        }
        composeRule.onNodeWithText("Khan Entrant").assertIsDisplayed()
        scenario.close()
    }

    /*
    * This test will click a profile and can see the profile details
    */
    @Test
    fun admin_seeFull_Profile() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openProfilesTab()
        openProfileDialog("entrant-1")
        composeRule.onNodeWithText("Email: khan@gmail.com").assertExists()
        composeRule.onNodeWithText("587-599-2459", substring = true).assertExists()
        scenario.close()
    }

    /*
    * This test will delete an account from the list.
    */
    @Test
    fun admin_deleteProfile_fromList() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openProfilesTab()
        openProfileDialog("entrant-1")
        delete_Account()
        waitFor_TabGone("admin_profile_item_entrant-1")
        runBlocking {
            TestCase.assertNull(userRepository.getUser("entrant-1"))
            TestCase.assertNull(profileRepository.getProfile("entrant-1"))
        }
        composeRule.onNodeWithTag("admin_profile_item_owner-1").assertIsDisplayed()
        scenario.close()
    }

    @Test
    fun admin_deleteProfile_CancelsActive_And_keepPastrecords() {
        runBlocking {
            reservationRepository.createReservation(
                Reservation(
                    reservationId = "res-active",
                    userId = "entrant-1",
                    campsiteId = "1",
                    reservationState = ReservationState.Reserved
                )
            )
            reservationRepository.createReservation(
                Reservation(
                    reservationId = "res-past",
                    userId = "entrant-1",
                    campsiteId = "1",
                    reservationState = ReservationState.Declined
                )
            )
            ratingRepository.createRating(
                Rating(
                    ratingId = "rating-1",
                    campsiteId = "1",
                    userId = "entrant-1",
                    rating = 5,
                    review = "Nice Spot Man"
                )
            )
        }
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openProfilesTab()
        openProfileDialog("entrant-1")
        delete_Account()
        waitFor_TabGone("admin_profile_item_entrant-1")

        runBlocking {
            val active = reservationRepository.getReservation("res-active")
            val past = reservationRepository.getReservation("res-past")
            val rating = ratingRepository.getRatings()?.find { it.ratingId == "rating-1" }
            TestCase.assertNotNull(active)
            TestCase.assertNotNull(past)
            TestCase.assertNotNull(rating)
            TestCase.assertEquals(ReservationState.Canceled, active!!.reservationState)
            TestCase.assertEquals(ReservationState.Declined, past!!.reservationState)
            TestCase.assertEquals("deleted-user", active.userId)
            TestCase.assertEquals("deleted-user", past.userId)
            TestCase.assertEquals("deleted-user", rating!!.userId)
        }
        scenario.close()
    }

    @Test
    fun admin_cannotDeleteTheirOwnProfile_FromList(){
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        openProfilesTab()
        openProfileDialog("admin-user")
        composeRule.onNodeWithTag("admin_profile_detail").assertIsDisplayed()
        composeRule.onNodeWithText("Delete Account", ignoreCase = true).assertDoesNotExist()
        scenario.close()
    }
}