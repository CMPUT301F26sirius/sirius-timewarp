package com.example.timewarp.feature.moderate

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeRatingRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.example.timewarp.domain.repository.UserRepository
import com.example.timewarp.feature.campsite.details.CampsiteDetailsScreen
import com.example.timewarp.ui.theme.TimewarpTheme
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
class AdminRatingTest {
    @get:
    Rule(order = 0) val hiltRule = HiltAndroidRule(this)

    @get:
    Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var campsiteRepository: FakeCampsiteRepository

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var ratingRepository: RatingRepository

    /*
    * This function signs in as admin and seeds an admin and entrant and clears old ratings
    */
    @Before
    fun setUp() {
        hiltRule.inject()
        (userRepository as FakeUserRepository).clear()
        (ratingRepository as FakeRatingRepository).clear()
        (authRepository as FakeAuthRepository).setCurrentUserId("admin-user")

        val users = userRepository as FakeUserRepository
        users.addUser(User(userId = "admin-user", userType = UserType.Admin))
        users.addUser(User(userId = "entrant-1", userType = UserType.Entrant))
        users.addUser(User(userId = "owner-1", userType = UserType.CampsiteOwner))

        val campsites = campsiteRepository as FakeCampsiteRepository
        campsites.clear()
        campsites.addCampsite(
            Campsite(
                campsiteId = "1",
                name = "Test Campsite",
                ownerUserId = "owner-1"
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
    * This function seed one rating from entrant-1 on campsite 1
    */
    private fun seedRating(review: String = "Terrible, avoid this place") = runBlocking{
        ratingRepository.createRating(
            Rating(
                ratingId = "rating-1",
                campsiteId = "1",
                userId = "entrant-1",
                rating = 4,
                review = review
            )
        )
    }

    /*
    * This function will open the campsite details screen for campsite 1 and then waits for
    * the rating
    */
    private fun openCampsiteDetails(): ActivityScenario<MainActivity> {
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity {
            it.setContent {
                TimewarpTheme { CampsiteDetailsScreen(campsiteId = "1") }
            }
        }
        waitFor_Text("4/5")
        return scenario
    }

    /*
    * This function tests remove ratings and reviews.
    * Cancel will keep the rating and removing keeps the stars and remove rating deletes it.
    */
    @Test
    fun admin_canRemove_Review_and_Ratings() {
        seedRating()
        val scenario = openCampsiteDetails()

        // Press cancel and keep the rating
        composeRule.onNodeWithTag("remove_rating_rating-1").performScrollTo().performClick()
        waitFor_Tag("confirm_remove_rating")
        composeRule.onNodeWithText("Cancel").performClick()
        waitFor_TabGone("confirm_remove_rating")
        composeRule.onNodeWithTag("details_rating_count").assertTextEquals("1 reviews")

        // When you remove the review the text is gone
        composeRule.onNodeWithTag("remove_review_rating-1").performScrollTo().performClick()
        waitFor_Tag("confirm_remove_review")
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_remove_review").performClick()
        waitFor_TabGone("review_item")
        composeRule.onNodeWithText("4/5", substring = true).assertExists()
        composeRule.onNodeWithTag("remove_review_rating-1").assertDoesNotExist()
        runBlocking {
            TestCase.assertEquals(
                "",
                ratingRepository.getRating("rating-1")!!.review
            )
        }

        // removes rating
        composeRule.onNodeWithTag("remove_rating_rating-1").performScrollTo().performClick()
        waitFor_Tag("confirm_remove_rating")
        composeRule.onNodeWithTag("confirm_remove_rating").performClick()
        waitFor_TabGone("remove_rating_rating-1")
        composeRule.onNodeWithTag("details_rating_count").assertTextEquals("0 reviews")
        runBlocking {
            TestCase.assertNull(ratingRepository.getRating("rating-1"))
        }
        scenario.close()
    }

    @Test
    fun entrant_cannotSee_RemoveButton() {
        (authRepository as FakeAuthRepository).setCurrentUserId("entrant-1")
        seedRating()
        val scenario = openCampsiteDetails()

        composeRule.onNodeWithTag("remove_rating_rating-1").assertDoesNotExist()
        composeRule.onNodeWithTag("remove_rating_rating-1").assertDoesNotExist()
        scenario.close()
    }

}