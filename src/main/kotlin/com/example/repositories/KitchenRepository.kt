package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.KitchenOrderTable
import com.example.database.KitchenOrderItemTable
import com.example.models.*
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class KitchenRepository {
    
    suspend fun createKitchenOrder(order: KitchenOrder): KitchenOrder = dbQuery {
        val insertStatement = KitchenOrderTable.insert {
            it[orderId] = order.orderId
            it[tableNumber] = order.tableNumber
            it[status] = order.status
            it[priority] = order.priority
            it[estimatedPrepTime] = order.estimatedPrepTime
            it[startedAt] = LocalDateTime.now().toString()
        }
        
        val kitchenOrderId = insertStatement.resultedValues?.first()?.get(KitchenOrderTable.id)
            ?: throw Exception("Failed to create kitchen order")

        // Insert kitchen order items
        order.items.forEach { item ->
            KitchenOrderItemTable.insert {
                it[KitchenOrderItemTable.kitchenOrderId] = kitchenOrderId
                it[menuItemName] = item.menuItemName
                it[quantity] = item.quantity
                it[specialInstructions] = item.specialInstructions
                it[status] = item.status
            }
        }

        getKitchenOrderById(kitchenOrderId) ?: throw Exception("Failed to retrieve created kitchen order")
    }

    suspend fun getKitchenOrderById(id: Int): KitchenOrder? = dbQuery {
        val order = KitchenOrderTable.select { KitchenOrderTable.id eq id }
            .singleOrNull() ?: return@dbQuery null

        val items = KitchenOrderItemTable
            .select { KitchenOrderItemTable.kitchenOrderId eq id }
            .map { row ->
                KitchenOrderItem(
                    id = row[KitchenOrderItemTable.id],
                    kitchenOrderId = row[KitchenOrderItemTable.kitchenOrderId],
                    menuItemName = row[KitchenOrderItemTable.menuItemName],
                    quantity = row[KitchenOrderItemTable.quantity],
                    specialInstructions = row[KitchenOrderItemTable.specialInstructions],
                    status = row[KitchenOrderItemTable.status]
                )
            }

        KitchenOrder(
            id = order[KitchenOrderTable.id],
            orderId = order[KitchenOrderTable.orderId],
            tableNumber = order[KitchenOrderTable.tableNumber],
            items = items,
            status = order[KitchenOrderTable.status],
            priority = order[KitchenOrderTable.priority],
            estimatedPrepTime = order[KitchenOrderTable.estimatedPrepTime],
            startedAt = order[KitchenOrderTable.startedAt],
            completedAt = order[KitchenOrderTable.completedAt]
        )
    }

    suspend fun updateKitchenOrderStatus(id: Int, statusUpdate: KitchenOrderStatusUpdate): KitchenOrder? = dbQuery {
        val updatedRows = KitchenOrderTable.update({ KitchenOrderTable.id eq id }) {
            it[status] = statusUpdate.status
            it[estimatedPrepTime] = statusUpdate.estimatedPrepTime
            if (statusUpdate.status == KitchenOrderStatus.READY_TO_SERVE) {
                it[completedAt] = LocalDateTime.now().toString()
            }
        }

        if (updatedRows > 0) getKitchenOrderById(id) else null
    }

    suspend fun getActiveKitchenOrders(): List<KitchenOrder> = dbQuery {
        val activeStatuses = listOf(
            KitchenOrderStatus.NEW,
            KitchenOrderStatus.IN_PREPARATION,
            KitchenOrderStatus.READY_TO_SERVE
        )
        
        KitchenOrderTable
            .select { KitchenOrderTable.status inList activeStatuses }
            .orderBy(KitchenOrderTable.priority to SortOrder.DESC)
            .map { order ->
                val items = KitchenOrderItemTable
                    .select { KitchenOrderItemTable.kitchenOrderId eq order[KitchenOrderTable.id] }
                    .map { row ->
                        KitchenOrderItem(
                            id = row[KitchenOrderItemTable.id],
                            kitchenOrderId = row[KitchenOrderItemTable.kitchenOrderId],
                            menuItemName = row[KitchenOrderItemTable.menuItemName],
                            quantity = row[KitchenOrderItemTable.quantity],
                            specialInstructions = row[KitchenOrderItemTable.specialInstructions],
                            status = row[KitchenOrderItemTable.status]
                        )
                    }

                KitchenOrder(
                    id = order[KitchenOrderTable.id],
                    orderId = order[KitchenOrderTable.orderId],
                    tableNumber = order[KitchenOrderTable.tableNumber],
                    items = items,
                    status = order[KitchenOrderTable.status],
                    priority = order[KitchenOrderTable.priority],
                    estimatedPrepTime = order[KitchenOrderTable.estimatedPrepTime],
                    startedAt = order[KitchenOrderTable.startedAt],
                    completedAt = order[KitchenOrderTable.completedAt]
                )
            }
    }
} 