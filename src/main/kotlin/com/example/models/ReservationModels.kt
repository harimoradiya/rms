package com.example.models

import kotlinx.serialization.Serializable

@Serializable
enum class ReservationStatus {
    PENDING, CONFIRMED, CANCELLED, COMPLETED
}

@Serializable
data class ReservationRequest(
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val numberOfGuests: Int,
    val reservationDate: String,
    val reservationTime: String,
    val specialRequests: String? = null
)

@Serializable
data class Reservation(
    val id: Int = 0,
    val customerName: String,
    val customerEmail: String,
    val customerPhone: String,
    val numberOfGuests: Int,
    val reservationDate: String,
    val reservationTime: String,
    val status: ReservationStatus,
    val specialRequests: String? = null
) 