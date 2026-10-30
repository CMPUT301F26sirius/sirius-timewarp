package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Lottery

interface LotteryRepository {
    suspend fun getLotteries(): MutableList<Lottery>?

    suspend fun createLottery(lottery: Lottery)

    suspend fun editLottery(lotteryId: String, lottery: Lottery)

    suspend fun getLottery(lotteryId: String): Lottery?
}