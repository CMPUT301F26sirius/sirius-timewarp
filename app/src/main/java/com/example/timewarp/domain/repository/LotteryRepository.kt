package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Lottery
import kotlinx.coroutines.flow.Flow

/**
 * [LotteryRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface LotteryRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getLotteries(): MutableList<Lottery>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun createLottery(lottery: Lottery)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun editLottery(lotteryId: String, lottery: Lottery)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getLottery(lotteryId: String): Lottery?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>>
}