package com.example.timewarp.feature.lottery.form

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.timewarp.domain.model.Campsite
import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import com.example.timewarp.feature.lottery.create.CreateLotteryState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus

enum class LotteryFormStep {Campsites, Details}

class LotteryFormState {

    var step by mutableStateOf(LotteryFormStep.Campsites)
    var selectedCampsites by mutableStateOf<List<Campsite>>(emptyList())

    var winnerCount by mutableStateOf<Int?>(Lottery().winnerCount)
    var waitlistCount by mutableStateOf<Int?>(Lottery().waitlistCapacity)

    var registrationPeriod by mutableStateOf<Period?>(null)
    var bookingPeriod by mutableStateOf<Period?>(null)

    val isValid: Boolean
        get() = selectedCampsites.isNotEmpty() &&
                (winnerCount ?:0) >= 1 &&
                (waitlistCount ?: -1) >= 0 &&
                registrationPeriod != null &&
                bookingPeriod != null

    fun toggleCampsite(campsite: Campsite) {
        selectedCampsites =
            if (campsite in selectedCampsites) {
                selectedCampsites - campsite
            } else {
                selectedCampsites + campsite
            }
    }

    fun onWinnerCountChange(text: String) {
        winnerCount = text.toIntOrNull()
    }

    fun onWaitListCountChange(text: String) {
        waitlistCount = text.toIntOrNull()
    }

    fun onRegistrationPeriodChange(period: Period?) {
        registrationPeriod = period
    }

    fun onBookingPeriodChange(period: Period?) {
        bookingPeriod = period
    }

    fun fillExistingFields(lottery: Lottery, ownedCampsites: List<Campsite>) {
        selectedCampsites = ownedCampsites.filter { it.campsiteId in lottery.campsiteIds }

        winnerCount = lottery.winnerCount
        waitlistCount = lottery.waitlistCapacity
        registrationPeriod = lottery.registrationPeriod
        bookingPeriod = lottery.bookingPeriod

    }
}