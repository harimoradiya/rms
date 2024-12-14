package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: Int? = null,
    val tableId: Int,
    val sessionId: String,
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double,
    val createdAt: String? = null,
    val items: List<OrderItem>
)

@Serializable
data class OrderItem(
    val id: Int? = null,
    val orderId: Int,
    val menuItemId: Int,
    val menuItemName: String? = null,
    val quantity: Int,
    val itemPrice: Double,
    val specialInstructions: String? = null
)

@Serializable
enum class OrderStatus {
    PENDING, PREPARING, READY, SERVED, CANCELLED
}

@Serializable
data class CreateOrderRequest(
    val tableId: Int,
    val items: List<CreateOrderItemRequest>
)

@Serializable
data class CreateOrderItemRequest(
    val menuItemId: Int,
    val quantity: Int,
    val specialInstructions: String? = null
)

@Serializable
data class AddOrderItemRequest(
    val menuItemId: Int,
    val quantity: Int,
    val specialInstructions: String? = null
) 