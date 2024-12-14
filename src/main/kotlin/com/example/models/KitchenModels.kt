package com.example.models

import kotlinx.serialization.Serializable

@Serializable
enum class KitchenOrderStatus {
    NEW,
    IN_PREPARATION,
    READY_TO_SERVE,
    SERVED,
    CANCELLED
}

@Serializable
data class KitchenOrder(
    val id: Int = 0,
    val orderId: Int,
    val tableNumber: Int,
    val items: List<KitchenOrderItem>,
    val status: KitchenOrderStatus,
    val priority: Int = 1,
    val estimatedPrepTime: Int? = null,
    val startedAt: String? = null,
    val completedAt: String? = null
)

@Serializable
data class KitchenOrderItem(
    val id: Int = 0,
    val kitchenOrderId: Int = 0,
    val menuItemName: String,
    val quantity: Int,
    val specialInstructions: String? = null,
    val status: KitchenOrderStatus
)

@Serializable
data class KitchenOrderStatusUpdate(
    val status: KitchenOrderStatus,
    val estimatedPrepTime: Int? = null
) 