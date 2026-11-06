package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Lottery
import kotlinx.coroutines.flow.Flow

/**
 * Repository for storing and observing [Lottery] objects.
 */
interface LotteryRepository {
    /**
     * @return all lotteries, or `null` when no collection result is available.
     */
    suspend fun getLotteries(): MutableList<Lottery>?

    /**
     * @param lottery lottery to create.
     */
    suspend fun createLottery(lottery: Lottery)

    /**
     * @param lotteryId identifier of the lottery to edit.
     * @param lottery replacement lottery data.
     */
    suspend fun editLottery(lotteryId: String, lottery: Lottery)

    /**
     * @param lotteryId identifier of the lottery to retrieve.
     * @return the matching lottery, or `null` when it does not exist.
     */
    suspend fun getLottery(lotteryId: String): Lottery?

    /**
     * @param campsiteId identifier of the campsite whose lotteries are observed.
     * @return a flow emitting lotteries associated with the campsite.
     */
    fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>>
}