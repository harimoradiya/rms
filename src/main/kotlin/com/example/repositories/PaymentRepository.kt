package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.OrderTable
import com.example.database.PaymentTable
import com.example.database.RestaurantTable
import com.example.models.*
import org.jetbrains.exposed.sql.*
import java.time.LocalDateTime

class PaymentRepository {
    
    suspend fun createPayment(payment: Payment): Payment = dbQuery {
        val insertStatement = PaymentTable.insert {
            it[orderId] = payment.orderId
            it[amount] = payment.amount
            it[paymentMethod] = payment.paymentMethod
            it[paymentStatus] = payment.paymentStatus
            it[transactionReference] = payment.transactionReference
            it[createdAt] = LocalDateTime.now().toString()
        }
        
        val resultRow = insertStatement.resultedValues?.first()
        resultRow?.let {
            Payment(
                id = it[PaymentTable.id],
                orderId = it[PaymentTable.orderId],
                amount = it[PaymentTable.amount],
                paymentMethod = it[PaymentTable.paymentMethod],
                paymentStatus = it[PaymentTable.paymentStatus],
                transactionReference = it[PaymentTable.transactionReference],
                createdAt = it[PaymentTable.createdAt]
            )
        } ?: throw Exception("Failed to create payment")
    }

    suspend fun getPaymentByOrderId(orderId: Int): Payment? = dbQuery {
        PaymentTable.select { PaymentTable.orderId eq orderId }
            .mapNotNull { row ->
                Payment(
                    id = row[PaymentTable.id],
                    orderId = row[PaymentTable.orderId],
                    amount = row[PaymentTable.amount],
                    paymentMethod = row[PaymentTable.paymentMethod],
                    paymentStatus = row[PaymentTable.paymentStatus],
                    transactionReference = row[PaymentTable.transactionReference],
                    createdAt = row[PaymentTable.createdAt]
                )
            }
            .singleOrNull()
    }

    suspend fun getTablePayments(tableId: Int, sessionId: String? = null): TablePaymentSummary = dbQuery {
        // Get table details
        val table = RestaurantTable
            .select { RestaurantTable.id eq tableId }
            .singleOrNull() ?: throw Exception("Table not found")

        // Get active session if not provided
        val activeSessionId = sessionId ?: OrderTable
            .slice(OrderTable.sessionId)
            .select { 
                (OrderTable.tableId eq tableId) and
                (OrderTable.status neq OrderStatus.SERVED) and
                (OrderTable.status neq OrderStatus.CANCELLED)
            }
            .orderBy(OrderTable.createdAt to SortOrder.DESC)
            .limit(1)
            .map { it[OrderTable.sessionId] }
            .firstOrNull() ?: throw Exception("No active session found")

        // Get all orders for this session
        val orders = OrderTable
            .select { 
                (OrderTable.tableId eq tableId) and 
                (OrderTable.sessionId eq activeSessionId) 
            }
            .map { it[OrderTable.id] }

        // Get all payments for these orders
        val payments = PaymentTable
            .select { PaymentTable.orderId inList orders }
            .map { row ->
                Payment(
                    id = row[PaymentTable.id],
                    orderId = row[PaymentTable.orderId],
                    amount = row[PaymentTable.amount],
                    paymentMethod = row[PaymentTable.paymentMethod],
                    paymentStatus = row[PaymentTable.paymentStatus],
                    transactionReference = row[PaymentTable.transactionReference],
                    createdAt = row[PaymentTable.createdAt]
                )
            }

        // Calculate totals
        val totalAmount = OrderTable
            .slice(OrderTable.totalAmount.sum())
            .select { 
                (OrderTable.tableId eq tableId) and 
                (OrderTable.sessionId eq activeSessionId) 
            }
            .single()[OrderTable.totalAmount.sum()] ?: 0.0

        val paidAmount = payments
            .filter { it.paymentStatus == PaymentStatus.COMPLETED }
            .sumOf { it.amount }

        val remainingAmount = totalAmount - paidAmount

        val paymentStatus = when {
            remainingAmount <= 0 -> PaymentStatus.COMPLETED
            paidAmount > 0 -> PaymentStatus.PENDING
            else -> PaymentStatus.PENDING
        }

        TablePaymentSummary(
            tableId = tableId,
            tableNumber = table[RestaurantTable.tableNumber],
            sessionId = activeSessionId,
            totalAmount = totalAmount,
            paidAmount = paidAmount,
            remainingAmount = remainingAmount,
            paymentStatus = paymentStatus,
            payments = payments,
            lastPaymentAt = payments.maxByOrNull { it.createdAt ?: "" }?.createdAt
        )
    }
} 