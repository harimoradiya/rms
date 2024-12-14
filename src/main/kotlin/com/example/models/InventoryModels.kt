package com.example.models

import kotlinx.serialization.Serializable

@Serializable
enum class UnitType {
    KILOGRAM, GRAM, LITER, MILLILITER, PIECE, PACKET, BOX
}

@Serializable
enum class StockStatus {
    IN_STOCK, LOW_STOCK, OUT_OF_STOCK
}

@Serializable
data class InventoryItem(
    val id: Int = 0,
    val name: String,
    val description: String? = null,
    val quantity: Double,
    val unitType: UnitType,
    val minimumStockLevel: Double,
    val maximumStockLevel: Double,
    val status: StockStatus,
    val cost: Double,
    val supplier: String? = null,
    val location: String? = null,
    val lastRestockedAt: String? = null
)

@Serializable
data class InventoryItemRequest(
    val name: String,
    val description: String? = null,
    val quantity: Double,
    val unitType: UnitType,
    val minimumStockLevel: Double,
    val maximumStockLevel: Double,
    val cost: Double,
    val supplier: String? = null,
    val location: String? = null
)

@Serializable
data class StockUpdateRequest(
    val quantity: Double,
    val reason: String? = null
)

@Serializable
data class InventoryTransaction(
    val id: Int = 0,
    val itemId: Int,
    val type: TransactionType,
    val quantity: Double,
    val previousQuantity: Double,
    val newQuantity: Double,
    val reason: String? = null,
    val transactionDate: String? = null,
    val userId: Int? = null
)

@Serializable
enum class TransactionType {
    STOCK_IN, STOCK_OUT, ADJUSTMENT, WASTAGE
} 