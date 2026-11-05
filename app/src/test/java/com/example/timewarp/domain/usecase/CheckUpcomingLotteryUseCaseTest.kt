package com.example.timewarp.domain.usecase

import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Period
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test


/** Unit tests for [CheckUpcomingLotteryUseCase] (US 02.03.01). */
class CheckUpcomingLotteryUseCaseTest {

    // November 10, 2027 as a day number
    private val today = 21132

    private lateinit var checkUpcomingLottery: CheckUpcomingLotteryUseCase

    @Before
    fun setUp() {
        checkUpcomingLottery = CheckUpcomingLotteryUseCase()
    }

    // Lottery whose registration runs from start to end
    private fun lottery(start: Int, end: Int): Lottery {
        return Lottery(registrationPeriod = Period(start = start, end = end))
    }

    @Test
    fun registrationEndsLater_isUpcoming() {
        assertTrue(checkUpcomingLottery(listOf(lottery(today - 2, today + 5)), today))
    }

    @Test
    fun registrationNotStartedYet_isUpcoming() {
        assertTrue(checkUpcomingLottery(listOf(lottery(today + 3, today + 10)), today))
    }

    @Test
    fun registrationEndsToday_isUpcoming() {
        assertTrue(checkUpcomingLottery(listOf(lottery(today - 7, today)), today))
    }

    @Test
    fun registrationAlreadyEnded_isNotUpcoming() {
        assertFalse(checkUpcomingLottery(listOf(lottery(today - 10, today - 1)), today))
    }

    @Test
    fun noLotteries_isNotUpcoming() {
        assertFalse(checkUpcomingLottery(emptyList(), today))
    }

    @Test
    fun oneEndedOneUpcoming_isUpcoming() {
        val lotteries = listOf(lottery(today - 10, today - 1), lottery(today + 1, today + 7))
        assertTrue(checkUpcomingLottery(lotteries, today))
    }
}