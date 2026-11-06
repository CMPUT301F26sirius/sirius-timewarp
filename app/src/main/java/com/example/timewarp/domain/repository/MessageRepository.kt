package com.example.timewarp.domain.repository

import com.example.timewarp.domain.model.Message

/**
 * [MessageRepository] interface, with implementation in [com.example.timewarp.data.repository]
 */
interface MessageRepository {
    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getCorrespondance(fromUserId: String, toUserId: String): MutableList<Message>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getOutgoingMessages(userId: String): MutableList<Message>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getIncomingMessages(userId: String): MutableList<Message>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getMessages(): MutableList<Message>?

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun sendMessage(message: Message)

    /**
     * Implementation details specified in [com.example.timewarp.data.repository]
     */
    suspend fun getMessage(messageId: String): Message?
}