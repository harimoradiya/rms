package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackRequest(
    val orderId: Int,
    val rating: Int, // 1-5 stars
    val comment: String? = null,
    val foodQuality: Int? = null,
    val serviceQuality: Int? = null,
    val ambience: Int? = null,
    val cleanliness: Int? = null
)

@Serializable
data class Feedback(
    val id: Int = 0,
    val orderId: Int,
    val userId: Int? = null,
    val rating: Int,
    val comment: String? = null,
    val foodQuality: Int? = null,
    val serviceQuality: Int? = null,
    val ambience: Int? = null,
    val cleanliness: Int? = null,
    val createdAt: String? = null,
    val isAnonymous: Boolean = false
)

@Serializable
data class FeedbackSummary(
    val averageRating: Double,
    val totalFeedbacks: Int,
    val ratingDistribution: Map<Int, Int>, // Map of rating to count
    val averageFoodQuality: Double?,
    val averageServiceQuality: Double?,
    val averageAmbience: Double?,
    val averageCleanliness: Double?
) 