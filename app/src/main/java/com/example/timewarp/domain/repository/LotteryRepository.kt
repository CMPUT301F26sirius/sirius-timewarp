package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Lottery
import kotlinx.coroutines.flow.Flow

interface LotteryRepository {
    suspend fun getLotteries(): MutableList<Lottery>?

    suspend fun createLottery(lottery: Lottery)

    suspend fun editLottery(lotteryId: String, lottery: Lottery)

    suspend fun getLottery(lotteryId: String): Lottery?

    fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>>
}