package com.example.timewarp.feature.profile.update

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextReplacement
import com.example.timewarp.ui.theme.TimewarpTheme
import junit.framework.TestCase
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class UpdateProfileTest {
    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    private val startState = UpdateProfileState.Content(
        firstName = "Alex",
        lastName = "Smith",
        email = "alex@test.com",
        phoneNumber = "7801234567"
    )

    private var savedState: UpdateProfileState.Content? = null
    private var cancelled = false

    @Before
    fun showForm() {
        composeTestRule.setContent {
            // Holds state like the view model does, so typing updates the fields
            var state by remember { mutableStateOf(startState) }

            TimewarpTheme {
                UpdateProfileForm(
                    state = state,
                    onFirstNameChange = { state = state.copy(firstName = it) },
                    onLastNameChange = { state = state.copy(lastName = it) },
                    onEmailChange = { state = state.copy(email = it) },
                    onPhoneNumberChange = { state = state.copy(phoneNumber = it) },
                    onSave = { savedState = state },
                    onCancel = { cancelled = true }
                )
            }
        }
    }

    private fun field(tag: String) =
        composeTestRule.onNode(hasSetTextAction() and hasAnyAncestor(hasTestTag(tag)))

    private fun updateButton() =
        composeTestRule.onNodeWithTag("updateButton").performScrollTo()

    private fun cancelButton() =
        composeTestRule.onNodeWithTag("cancelButton").performScrollTo()

    // Screen shows up

    @Test
    fun screenShowsFieldsAndButtons() {
        composeTestRule.onNodeWithText("Enter your details").assertIsDisplayed()

        field("updateFirstName").assertIsDisplayed()
        field("updateLastName").assertIsDisplayed()
        field("updateEmail").assertIsDisplayed()
        field("updatePhoneNumber").assertIsDisplayed()

        updateButton().assertIsDisplayed()
        cancelButton().assertIsDisplayed()
    }

    @Test
    fun screenShowsCurrentProfileValues() {
        field("updateFirstName").assertTextContains("Alex")
        field("updateLastName").assertTextContains("Smith")
        field("updateEmail").assertTextContains("alex@test.com")
        field("updatePhoneNumber").assertTextContains("7801234567")
    }

    // Entering inputs

    @Test
    fun enteringInputsUpdatesFields() {
        field("updateFirstName").performTextReplacement("Jordan")
        field("updateLastName").performTextReplacement("Lee")
        field("updateEmail").performTextReplacement("jordan@test.com")
        field("updatePhoneNumber").performTextReplacement("5879998888")

        field("updateFirstName").assertTextContains("Jordan")
        field("updateLastName").assertTextContains("Lee")
        field("updateEmail").assertTextContains("jordan@test.com")
        field("updatePhoneNumber").assertTextContains("5879998888")
    }

    // Updating

    @Test
    fun updateSendsEnteredValues() {
        field("updateFirstName").performTextReplacement("Jordan")
        field("updateEmail").performTextReplacement("jordan@test.com")

        updateButton().performClick()

        val saved = savedState!!
        TestCase.assertEquals("Jordan", saved.firstName)
        TestCase.assertEquals("Smith", saved.lastName)
        TestCase.assertEquals("jordan@test.com", saved.email)
        TestCase.assertEquals("7801234567", saved.phoneNumber)
    }

    @Test
    fun updateDisabledWhenRequiredFieldEmpty() {
        updateButton().assertIsEnabled()

        field("updateFirstName").performTextClearance()

        updateButton().assertIsNotEnabled()
    }

    @Test
    fun cancelDoesNotSave() {
        field("updateFirstName").performTextReplacement("Jordan")

        cancelButton().performClick()

        TestCase.assertTrue(cancelled)
        TestCase.assertNull(savedState)
    }

}