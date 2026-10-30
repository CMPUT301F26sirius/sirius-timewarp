package com.example.timewarp.data.repository

import com.example.timewarp.domain.model.Lottery
import com.example.timewarp.domain.model.Rating
import com.example.timewarp.domain.repository.LotteryRepository
import com.example.timewarp.domain.repository.RatingRepository
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Firebase-backed implementation of [LotteryRepository].
 *
 * Stores and retrieves lotteries from the Firestore `lottery` collection.
 *
 * @property firestore Firestore instance used to access user documents.
 */
@Singleton
class FirebaseLotteryRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : LotteryRepository {
    override suspend fun createLottery(lottery: Lottery) {
        TODO("Not yet implemented")
    }

    override suspend fun editLottery(lotteryId: String, lottery: Lottery) {
        TODO("Not yet implemented")
    }

    override suspend fun getLotteries(): MutableList<Lottery>? {
        TODO("Not yet implemented")
    }

    override suspend fun getLottery(lotteryId: String): Lottery? {
        TODO("Not yet implemented")
    }
}