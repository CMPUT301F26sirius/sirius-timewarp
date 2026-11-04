package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.repository.LotteryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [LotteryRepository].
 *
 * Stores and retrieves lotteries from a `lottery` collection.
 */
@Singleton
class FakeLotteryRepository @Inject constructor() : LotteryRepository {
    private val lotteries = MutableStateFlow<List<Lottery>>(emptyList())

    fun clear() {
        lotteries.value = emptyList()
    }
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createLottery(lottery: Lottery) {
        lotteries.value = lotteries.value + lottery
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editLottery(lotteryId: String, lottery: Lottery) {
        lotteries.value = lotteries.value.map { if (it.lotteryId == lotteryId) lottery else it }
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLotteries(): MutableList<Lottery>? {
        return lotteries.value.toMutableList()
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLottery(lotteryId: String): Lottery? {
        return lotteries.value.firstOrNull { it.lotteryId == lotteryId }
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>> =
        lotteries.map { items -> items.filter { campsiteId in it.campsiteIds } }
}