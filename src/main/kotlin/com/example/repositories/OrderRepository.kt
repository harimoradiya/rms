package com.example.repositories

import com.example.database.DatabaseFactory
import com.example.database.DatabaseFactory.dbQuery
import com.example.database.MenuItemTable
import com.example.database.OrderItemTable
import com.example.database.OrderTable
import com.example.models.AddOrderItemRequest
import com.example.models.CreateOrderRequest
import com.example.models.Order
import com.example.models.OrderItem
import com.example.models.OrderStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class OrderRepository {

    suspend fun createOrder(request: CreateOrderRequest): Order = DatabaseFactory.dbQuery {
        try {
            // First get menu items to calculate total
            val menuItems = MenuItemTable
                .select { MenuItemTable.id inList request.items.map { it.menuItemId } }
                .associate { 
                    it[MenuItemTable.id] to MenuItemPrice(
                        name = it[MenuItemTable.name],
                        price = it[MenuItemTable.price].toDouble()
                    )
                }

            // Get existing session ID or generate new one
            val sessionId = getActiveSessionId(request.tableId) ?: generateSessionId(request.tableId)

            // Calculate total amount
            val totalAmount = request.items.sumOf { item ->
                val menuItem = menuItems[item.menuItemId] 
                    ?: throw Exception("Menu item not found")
                menuItem.price * item.quantity
            }

            // Create order
            val insertStatement = OrderTable.insert {
                it[tableId] = request.tableId
                it[status] = OrderStatus.PENDING
                it[this.totalAmount] = totalAmount
                it[this.sessionId] = sessionId
                it[createdAt] = LocalDateTime.now().toString()
            }
            
            val orderId = insertStatement.resultedValues?.first()?.get(OrderTable.id)
                ?: throw Exception("Failed to create order")

            // Create order items
            val orderItems = request.items.map { item ->
                val menuItem = menuItems[item.menuItemId]!!
                OrderItemTable.insert {
                    it[this.orderId] = orderId
                    it[menuItemId] = item.menuItemId
                    it[quantity] = item.quantity
                    it[itemPrice] = menuItem.price.toFloat()
                    it[specialInstructions] = item.specialInstructions ?: ""
                }.resultedValues?.first()?.let { row ->
                    OrderItem(
                        id = row[OrderItemTable.id],
                        orderId = orderId,
                        menuItemId = item.menuItemId,
                        menuItemName = menuItem.name,
                        quantity = item.quantity,
                        itemPrice = menuItem.price,
                        specialInstructions = item.specialInstructions
                    )
                } ?: throw Exception("Failed to create order item")
            }

            Order(
                id = orderId,
                tableId = request.tableId,
                sessionId = sessionId,
                status = OrderStatus.PENDING,
                totalAmount = totalAmount,
                createdAt = LocalDateTime.now().toString(),
                items = orderItems
            )
        } catch (e: Exception) {
            throw Exception("Failed to create order: ${e.message}")
        }
    }

    private data class MenuItemPrice(val name: String, val price: Double)

    private fun generateSessionId(tableId: Int): String {
        val date = LocalDateTime.now().format(DateTimeFormatter.BASIC_ISO_DATE)
        return "T${tableId}_${date}_${System.currentTimeMillis()}"
    }

    suspend fun getOrderById(id: Int): Order? = DatabaseFactory.dbQuery {
        val orderRow = OrderTable
            .select { OrderTable.id eq id }
            .singleOrNull() ?: return@dbQuery null

        val items = (OrderItemTable innerJoin MenuItemTable)
            .select { OrderItemTable.orderId eq id }
            .map { row ->
                OrderItem(
                    id = row[OrderItemTable.id],
                    orderId = row[OrderItemTable.orderId],
                    menuItemId = row[OrderItemTable.menuItemId],
                    menuItemName = row[MenuItemTable.name],
                    quantity = row[OrderItemTable.quantity],
                    itemPrice = row[OrderItemTable.itemPrice].toDouble(),
                    specialInstructions = row[OrderItemTable.specialInstructions]
                )
            }

        Order(
            id = orderRow[OrderTable.id],
            tableId = orderRow[OrderTable.tableId],
            status = orderRow[OrderTable.status],
            totalAmount = orderRow[OrderTable.totalAmount],
            createdAt = orderRow[OrderTable.createdAt],
            items = items,
            sessionId = orderRow[OrderTable.sessionId]
        )
    }

    suspend fun getAllOrders(): List<Order> = DatabaseFactory.dbQuery {
        OrderTable.selectAll().map { orderRow ->
            val items = OrderItemTable
                .select { OrderItemTable.orderId eq orderRow[OrderTable.id] }
                .map { itemRow ->
                    OrderItem(
                        id = itemRow[OrderItemTable.id],
                        orderId = itemRow[OrderItemTable.orderId],
                        menuItemId = itemRow[OrderItemTable.menuItemId],
                        quantity = itemRow[OrderItemTable.quantity],
                        itemPrice = itemRow[OrderItemTable.itemPrice].toDouble(),
                        specialInstructions = itemRow[OrderItemTable.specialInstructions]
                    )
                }

            Order(
                id = orderRow[OrderTable.id],
                tableId = orderRow[OrderTable.tableId],
                status = orderRow[OrderTable.status],
                totalAmount = orderRow[OrderTable.totalAmount],
                createdAt = orderRow[OrderTable.createdAt],
                items = items,
                sessionId = orderRow[OrderTable.sessionId]
            )
        }
    }

    suspend fun updateOrderStatus(id: Int, status: OrderStatus): Boolean = DatabaseFactory.dbQuery {
        OrderTable.update({ OrderTable.id eq id }) {
            it[OrderTable.status] = status
        } > 0
    }

    suspend fun deleteOrder(id: Int): Boolean = DatabaseFactory.dbQuery {
        // First delete all order items
        OrderItemTable.deleteWhere { orderId eq id }
        // Then delete the order
        OrderTable.deleteWhere { OrderTable.id eq id } > 0
    }

    suspend fun addOrderItem(orderId: Int, request: AddOrderItemRequest): OrderItem = DatabaseFactory.dbQuery {
        try {
            // Get menu item details
            val menuItem = MenuItemTable
                .select { MenuItemTable.id eq request.menuItemId }
                .singleOrNull()
                ?: throw Exception("Menu item not found")

            val price = menuItem[MenuItemTable.price].toDouble()

            // Create order item
            val insertStatement = OrderItemTable.insert {
                it[this.orderId] = orderId
                it[menuItemId] = request.menuItemId
                it[quantity] = request.quantity
                it[itemPrice] = price.toFloat()
                it[specialInstructions] = request.specialInstructions ?: ""
            }

            val resultRow = insertStatement.resultedValues?.first()
                ?: throw Exception("Failed to insert order item")

            // Update order total
            OrderTable.update({ OrderTable.id eq orderId }) {
                with(SqlExpressionBuilder) {
                    it[totalAmount] = totalAmount + (price * request.quantity)
                }
            }

            OrderItem(
                id = resultRow[OrderItemTable.id],
                orderId = orderId,
                menuItemId = request.menuItemId,
                menuItemName = menuItem[MenuItemTable.name],
                quantity = request.quantity,
                itemPrice = price,
                specialInstructions = request.specialInstructions
            )
        } catch (e: Exception) {
            throw Exception("Failed to add order item: ${e.message}")
        }
    }

    suspend fun deleteOrderItem(orderItemId: Int): Boolean = DatabaseFactory.dbQuery {
        OrderItemTable.deleteWhere { OrderItemTable.id eq orderItemId } > 0
    }

    suspend fun getOrdersByTableSession(tableId: Int, sessionId: String): List<Order> = DatabaseFactory.dbQuery {
        OrderTable
            .select { 
                (OrderTable.tableId eq tableId) and 
                (OrderTable.sessionId eq sessionId) 
            }
            .orderBy(OrderTable.createdAt)
            .map { orderRow ->
                val items = (OrderItemTable innerJoin MenuItemTable)
                    .select { OrderItemTable.orderId eq orderRow[OrderTable.id] }
                    .map { row ->
                        OrderItem(
                            id = row[OrderItemTable.id],
                            orderId = row[OrderItemTable.orderId],
                            menuItemId = row[OrderItemTable.menuItemId],
                            menuItemName = row[MenuItemTable.name],
                            quantity = row[OrderItemTable.quantity],
                            itemPrice = row[OrderItemTable.itemPrice].toDouble(),
                            specialInstructions = row[OrderItemTable.specialInstructions]
                        )
                    }
                
                Order(
                    id = orderRow[OrderTable.id],
                    tableId = orderRow[OrderTable.tableId],
                    sessionId = orderRow[OrderTable.sessionId],
                    status = orderRow[OrderTable.status],
                    totalAmount = orderRow[OrderTable.totalAmount],
                    createdAt = orderRow[OrderTable.createdAt],
                    items = items
                )
            }
    }

    // Get total bill for a session
    suspend fun getSessionTotal(tableId: Int, sessionId: String): Double = dbQuery {
        OrderTable
            .slice(OrderTable.totalAmount.sum())
            .select { 
                (OrderTable.tableId eq tableId) and 
                (OrderTable.sessionId eq sessionId) 
            }
            .single()[OrderTable.totalAmount.sum()] ?: 0.0
    }

    // Add this function to get active session ID
    private suspend fun getActiveSessionId(tableId: Int): String? = dbQuery {
        OrderTable
            .slice(OrderTable.sessionId)
            .select { 
                (OrderTable.tableId eq tableId) and
                (OrderTable.status neq OrderStatus.SERVED) and
                (OrderTable.status neq OrderStatus.CANCELLED)
            }
            .orderBy(OrderTable.createdAt to SortOrder.DESC)
            .limit(1)
            .map { it[OrderTable.sessionId] }
            .firstOrNull()
    }
}