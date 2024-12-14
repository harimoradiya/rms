package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.FeedbackTable
import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime

class FeedbackRepository {
    
    suspend fun createFeedback(feedback: Feedback): Feedback = dbQuery {
        val insertStatement = FeedbackTable.insert {
            it[orderId] = feedback.orderId
            it[userId] = feedback.userId
            it[rating] = feedback.rating
            it[comment] = feedback.comment
            it[foodQuality] = feedback.foodQuality
            it[serviceQuality] = feedback.serviceQuality
            it[ambience] = feedback.ambience
            it[cleanliness] = feedback.cleanliness
            it[createdAt] = LocalDateTime.now().toString()
            it[isAnonymous] = feedback.isAnonymous
        }
        
        val resultRow = insertStatement.resultedValues?.first()
        resultRow?.let {
            Feedback(
                id = it[FeedbackTable.id],
                orderId = it[FeedbackTable.orderId],
                userId = it[FeedbackTable.userId],
                rating = it[FeedbackTable.rating],
                comment = it[FeedbackTable.comment],
                foodQuality = it[FeedbackTable.foodQuality],
                serviceQuality = it[FeedbackTable.serviceQuality],
                ambience = it[FeedbackTable.ambience],
                cleanliness = it[FeedbackTable.cleanliness],
                createdAt = it[FeedbackTable.createdAt],
                isAnonymous = it[FeedbackTable.isAnonymous]
            )
        } ?: throw Exception("Failed to create feedback")
    }

    suspend fun getFeedbackByOrderId(orderId: Int): Feedback? = dbQuery {
        FeedbackTable.select { FeedbackTable.orderId eq orderId }
            .mapNotNull { row ->
                Feedback(
                    id = row[FeedbackTable.id],
                    orderId = row[FeedbackTable.orderId],
                    userId = row[FeedbackTable.userId],
                    rating = row[FeedbackTable.rating],
                    comment = row[FeedbackTable.comment],
                    foodQuality = row[FeedbackTable.foodQuality],
                    serviceQuality = row[FeedbackTable.serviceQuality],
                    ambience = row[FeedbackTable.ambience],
                    cleanliness = row[FeedbackTable.cleanliness],
                    createdAt = row[FeedbackTable.createdAt],
                    isAnonymous = row[FeedbackTable.isAnonymous]
                )
            }
            .singleOrNull()
    }

    suspend fun getFeedbackSummary(): FeedbackSummary = dbQuery {
        val feedbacks = FeedbackTable.selectAll().toList()
        
        val totalFeedbacks = feedbacks.size
        if (totalFeedbacks == 0) {
            return@dbQuery FeedbackSummary(
                averageRating = 0.0,
                totalFeedbacks = 0,
                ratingDistribution = emptyMap(),
                averageFoodQuality = null,
                averageServiceQuality = null,
                averageAmbience = null,
                averageCleanliness = null
            )
        }

        val ratingDistribution = feedbacks.groupBy { it[FeedbackTable.rating] }
            .mapValues { it.value.size }
        
        val averageRating = feedbacks.map { it[FeedbackTable.rating] }.average()
        val averageFoodQuality = feedbacks.mapNotNull { it[FeedbackTable.foodQuality] }
            .takeIf { it.isNotEmpty() }?.average()
        val averageServiceQuality = feedbacks.mapNotNull { it[FeedbackTable.serviceQuality] }
            .takeIf { it.isNotEmpty() }?.average()
        val averageAmbience = feedbacks.mapNotNull { it[FeedbackTable.ambience] }
            .takeIf { it.isNotEmpty() }?.average()
        val averageCleanliness = feedbacks.mapNotNull { it[FeedbackTable.cleanliness] }
            .takeIf { it.isNotEmpty() }?.average()

        FeedbackSummary(
            averageRating = averageRating,
            totalFeedbacks = totalFeedbacks,
            ratingDistribution = ratingDistribution,
            averageFoodQuality = averageFoodQuality,
            averageServiceQuality = averageServiceQuality,
            averageAmbience = averageAmbience,
            averageCleanliness = averageCleanliness
        )
    }

    suspend fun getRecentFeedbacks(limit: Int = 10): List<Feedback> = dbQuery {
        FeedbackTable
            .selectAll()
            .orderBy(FeedbackTable.createdAt to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                Feedback(
                    id = row[FeedbackTable.id],
                    orderId = row[FeedbackTable.orderId],
                    userId = row[FeedbackTable.userId],
                    rating = row[FeedbackTable.rating],
                    comment = row[FeedbackTable.comment],
                    foodQuality = row[FeedbackTable.foodQuality],
                    serviceQuality = row[FeedbackTable.serviceQuality],
                    ambience = row[FeedbackTable.ambience],
                    cleanliness = row[FeedbackTable.cleanliness],
                    createdAt = row[FeedbackTable.createdAt],
                    isAnonymous = row[FeedbackTable.isAnonymous]
                )
            }
    }
} 