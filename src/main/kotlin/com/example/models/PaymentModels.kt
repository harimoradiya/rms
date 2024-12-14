package com.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
enum class PaymentStatus {
    PENDING, COMPLETED, FAILED, REFUNDED
}

@Serializable
enum class PaymentMethod {
    CASH, CREDIT_CARD, DEBIT_CARD, UPI, WALLET
}

@Serializable
data class PaymentRequest(
    val orderId: Int,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val transactionReference: String? = null
)

@Serializable
data class Payment(
    val id: Int = 0,
    val orderId: Int,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val paymentStatus: PaymentStatus,
    val transactionReference: String? = null,
    val createdAt: String? = null
)

@Serializable
data class TablePaymentSummary(
    val tableId: Int,
    val tableNumber: Int,
    val sessionId: String,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentStatus: PaymentStatus,
    val payments: List<Payment>,
    val lastPaymentAt: String? = null
) 