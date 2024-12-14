package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.InventoryItemTable
import com.example.database.InventoryTransactionTable
import com.example.models.*
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class InventoryRepository {
    
    private fun calculateStockStatus(quantity: Double, minimumStockLevel: Double): StockStatus {
        return when {
            quantity <= 0 -> StockStatus.OUT_OF_STOCK
            quantity <= minimumStockLevel -> StockStatus.LOW_STOCK
            else -> StockStatus.IN_STOCK
        }
    }

    suspend fun createInventoryItem(item: InventoryItem): InventoryItem = dbQuery {
        val status = calculateStockStatus(item.quantity, item.minimumStockLevel)
        
        val insertStatement = InventoryItemTable.insert {
            it[name] = item.name
            it[description] = item.description
            it[quantity] = item.quantity
            it[unitType] = item.unitType
            it[minimumStockLevel] = item.minimumStockLevel
            it[maximumStockLevel] = item.maximumStockLevel
            it[this.status] = status
            it[cost] = item.cost
            it[supplier] = item.supplier
            it[location] = item.location
            it[lastRestockedAt] = LocalDateTime.now().toString()
        }

        val resultRow = insertStatement.resultedValues?.first()
            ?: throw Exception("Failed to create inventory item")

        InventoryItem(
            id = resultRow[InventoryItemTable.id],
            name = resultRow[InventoryItemTable.name],
            description = resultRow[InventoryItemTable.description],
            quantity = resultRow[InventoryItemTable.quantity],
            unitType = resultRow[InventoryItemTable.unitType],
            minimumStockLevel = resultRow[InventoryItemTable.minimumStockLevel],
            maximumStockLevel = resultRow[InventoryItemTable.maximumStockLevel],
            status = resultRow[InventoryItemTable.status],
            cost = resultRow[InventoryItemTable.cost],
            supplier = resultRow[InventoryItemTable.supplier],
            location = resultRow[InventoryItemTable.location],
            lastRestockedAt = resultRow[InventoryItemTable.lastRestockedAt]
        )
    }

    suspend fun updateStock(id: Int, update: StockUpdateRequest, userId: Int?): InventoryItem = dbQuery {
        val currentItem = InventoryItemTable.select { InventoryItemTable.id eq id }
            .firstOrNull() ?: throw Exception("Inventory item not found")

        val previousQuantity = currentItem[InventoryItemTable.quantity]
        val newQuantity = update.quantity
        val transactionType = if (newQuantity > previousQuantity) TransactionType.STOCK_IN else TransactionType.STOCK_OUT

        // Record transaction
        InventoryTransactionTable.insert {
            it[itemId] = id
            it[type] = transactionType
            it[quantity] = kotlin.math.abs(newQuantity - previousQuantity)
            it[this.previousQuantity] = previousQuantity
            it[this.newQuantity] = newQuantity
            it[reason] = update.reason
            it[transactionDate] = LocalDateTime.now().toString()
            it[this.userId] = userId
        }

        // Update stock
        val status = calculateStockStatus(newQuantity, currentItem[InventoryItemTable.minimumStockLevel])
        InventoryItemTable.update({ InventoryItemTable.id eq id }) {
            it[quantity] = newQuantity
            it[this.status] = status
            if (newQuantity > previousQuantity) {
                it[lastRestockedAt] = LocalDateTime.now().toString()
            }
        }

        getInventoryItemById(id) ?: throw Exception("Failed to retrieve updated inventory item")
    }

    suspend fun getInventoryItemById(id: Int): InventoryItem? = dbQuery {
        InventoryItemTable.select { InventoryItemTable.id eq id }
            .mapNotNull { row ->
                InventoryItem(
                    id = row[InventoryItemTable.id],
                    name = row[InventoryItemTable.name],
                    description = row[InventoryItemTable.description],
                    quantity = row[InventoryItemTable.quantity],
                    unitType = row[InventoryItemTable.unitType],
                    minimumStockLevel = row[InventoryItemTable.minimumStockLevel],
                    maximumStockLevel = row[InventoryItemTable.maximumStockLevel],
                    status = row[InventoryItemTable.status],
                    cost = row[InventoryItemTable.cost],
                    supplier = row[InventoryItemTable.supplier],
                    location = row[InventoryItemTable.location],
                    lastRestockedAt = row[InventoryItemTable.lastRestockedAt]
                )
            }
            .singleOrNull()
    }

    suspend fun getLowStockItems(): List<InventoryItem> = dbQuery {
        InventoryItemTable
            .select { InventoryItemTable.status eq StockStatus.LOW_STOCK }
            .map { row ->
                InventoryItem(
                    id = row[InventoryItemTable.id],
                    name = row[InventoryItemTable.name],
                    description = row[InventoryItemTable.description],
                    quantity = row[InventoryItemTable.quantity],
                    unitType = row[InventoryItemTable.unitType],
                    minimumStockLevel = row[InventoryItemTable.minimumStockLevel],
                    maximumStockLevel = row[InventoryItemTable.maximumStockLevel],
                    status = row[InventoryItemTable.status],
                    cost = row[InventoryItemTable.cost],
                    supplier = row[InventoryItemTable.supplier],
                    location = row[InventoryItemTable.location],
                    lastRestockedAt = row[InventoryItemTable.lastRestockedAt]
                )
            }
    }

    suspend fun getItemTransactions(itemId: Int, limit: Int = 10): List<InventoryTransaction> = dbQuery {
        InventoryTransactionTable
            .select { InventoryTransactionTable.itemId eq itemId }
            .orderBy(InventoryTransactionTable.transactionDate to SortOrder.DESC)
            .limit(limit)
            .map { row ->
                InventoryTransaction(
                    id = row[InventoryTransactionTable.id],
                    itemId = row[InventoryTransactionTable.itemId],
                    type = row[InventoryTransactionTable.type],
                    quantity = row[InventoryTransactionTable.quantity],
                    previousQuantity = row[InventoryTransactionTable.previousQuantity],
                    newQuantity = row[InventoryTransactionTable.newQuantity],
                    reason = row[InventoryTransactionTable.reason],
                    transactionDate = row[InventoryTransactionTable.transactionDate],
                    userId = row[InventoryTransactionTable.userId]
                )
            }
    }
} 