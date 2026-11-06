package com.example.timewarp.feature.campsite.create

import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.timewarp.MainActivity
import com.example.timewarp.data.repository.FakeAuthRepository
import com.example.timewarp.data.repository.FakeCampsiteRepository
import com.example.timewarp.data.repository.FakeLotteryRepository
import com.example.timewarp.data.repository.FakeUserRepository
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Location
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Occupancy
import com.example.timewarp.domain.model.Period
import com.example.timewarp.domain.model.User
import com.example.timewarp.domain.model.UserType
import com.example.timewarp.domain.repository.AuthRepository
import com.example.timewarp.domain.repository.CampsiteRepository
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.UserRepository
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

private const val ENTRANT_ID = "fake-entrant"
private const val CAMPSITE_ID = "test-campsite"
private const val LOTTERY_ID = "test-lottery"

/**
 * Instrumented tests for lottery details navigation.
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CreateCampsiteTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @get:Rule
    val composeRule = createEmptyComposeRule()

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var campsiteRepository: CampsiteRepository

    @Inject
    lateinit var lotteryRepository: LotteryRepository

    @Before
    fun setUp() {
        hiltRule.inject()

        (userRepository as FakeUserRepository).clear()
        (userRepository as FakeUserRepository).addUser(User(userId = ENTRANT_ID, userType = UserType.Entrant))
        (authRepository as FakeAuthRepository).setCurrentUserId(ENTRANT_ID)

        (campsiteRepository as FakeCampsiteRepository).clear()
        (campsiteRepository as FakeCampsiteRepository).addCampsite(
            Campsite(
                campsiteId = CAMPSITE_ID,
                name = "Test Campsite",
                location = Location(address = "Test address"),
                occupancy = Occupancy(maxGuests = 4)
            )
        )

        (lotteryRepository as FakeLotteryRepository).clear()
        (lotteryRepository as FakeLotteryRepository).addLottery(
            Lottery(
                lotteryId = LOTTERY_ID,
                registrationPeriod = Period(start = 0, end = Int.MAX_VALUE),
                campsiteIds = listOf(CAMPSITE_ID)
            )
        )
    }
}