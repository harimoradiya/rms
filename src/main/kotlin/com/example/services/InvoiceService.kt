package com.example.services

import com.example.models.*
import com.example.repositories.OrderRepository
import com.example.repositories.TableRepository
import com.example.utils.PDFGenerator
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InvoiceService(
    private val orderRepository: OrderRepository,
    private val tableRepository: TableRepository,
    private val pdfGenerator: PDFGenerator = PDFGenerator()
) {
    private val restaurantDetails = RestaurantDetails()

    suspend fun generateInvoicePDF(orderId: Int): ByteArray {
        val order = orderRepository.getOrderById(orderId)
            ?: throw Exception("Order not found")

        val invoiceDetails = InvoiceDetails(
            orderId = order.id!!,
            orderDate = order.createdAt ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            tableNumber = order.tableId,
            items = order.items,
            subtotal = order.items.sumOf { it.quantity * it.itemPrice },
            tax = order.items.sumOf { it.quantity * it.itemPrice } * 0.1, // 10% tax
            total = order.totalAmount,
            restaurantDetails = restaurantDetails
        )

        return pdfGenerator.generateInvoice(invoiceDetails)
    }

    suspend fun generateTableSessionInvoice(tableId: Int, sessionId: String): ByteArray {
        // Get table details
        val table = tableRepository.getTableById(tableId)
            ?: throw Exception("Table not found")

        // Get all orders for this table session
        val orders = orderRepository.getOrdersByTableSession(tableId, sessionId)
        if (orders.isEmpty()) {
            throw Exception("No orders found for this table session")
        }

        // Calculate totals
        val subtotal = orders.sumOf { it.totalAmount }
        val tax = subtotal * 0.1

        val invoice = TableSessionInvoice(
            tableId = tableId,
            tableNumber = table.tableNumber,
            sessionId = sessionId,
            startTime = orders.firstOrNull()?.createdAt 
                ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            orders = orders,
            subtotal = subtotal,
            tax = tax,
            total = subtotal + tax,
            restaurantDetails = restaurantDetails
        )

        return pdfGenerator.generateTableSessionInvoice(invoice)
    }
} 