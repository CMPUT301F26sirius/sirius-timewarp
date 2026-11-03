package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.repository.LotteryRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Fake implementation of [LotteryRepository].
 *
 * Stores and retrieves lotteries from a `lottery` collection.
 */
@Singleton
class FakeLotteryRepository @Inject constructor() : LotteryRepository {
    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun createLottery(lottery: Lottery) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun editLottery(lotteryId: String, lottery: Lottery) {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLotteries(): MutableList<Lottery>? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override suspend fun getLottery(lotteryId: String): Lottery? {
        TODO("Not yet implemented")
    }

    /**
     * NEEDS_DOCUMENTATION
     */
    override fun observeCampsiteLotteries(campsiteId: String): Flow<List<Lottery>> = callbackFlow {
        TODO("Not yet implemented")
    }
}