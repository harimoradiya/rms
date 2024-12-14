package com.example.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal


@Serializable
data class Table(
    val id : Int? = null,
    val tableNumber : Int,
    val capacity : Int,
    val status : TableStatus
)

@Serializable
data class MenuItem(
    val id: Int? = null,
    val name: String,
    val description : String,
    val price : String,
    val category: String,
    val isAvailable : Boolean = true
)

@Serializable
enum class TableStatus {
    AVAILABLE, OCCUPIED , RESERVED
}


@Serializable
enum class UserRole{
    ADMIN, MANAGER, STAFF,CUSTOMER
}

enum class OrderType{
    DINE_IN, TAKEAWAY , DELIVERY
}

@Serializable
data class OrderRequest(
    val tableNumber: Int,
    val items: List<OrderItemRequest>
)

@Serializable
data class OrderItemRequest(
    val menuItemId: Int,
    val quantity: Int,
    val specialInstructions: String?
)

@Serializable
data class StatusUpdateRequest(val status: String)



