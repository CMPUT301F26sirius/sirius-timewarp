package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Message

interface MessageRepository {
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Message>?

    suspend fun getOutgoingMessages(userId: String): MutableList<Message>?

    suspend fun getIncomingMessages(userId: String): MutableList<Message>?

    suspend fun getMessages(): MutableList<Message>?

    suspend fun sendMessage(message: Message)

    suspend fun getMessage(messageId: String): Message?
}