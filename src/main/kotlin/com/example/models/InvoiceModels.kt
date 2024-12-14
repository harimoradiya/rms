package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDetails(
    val orderId: Int,
    val orderDate: String,
    val customerName: String? = null,
    val tableNumber: Int,
    val items: List<OrderItem>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val paymentMethod: PaymentMethod? = null,
    val restaurantDetails: RestaurantDetails
)

@Serializable
data class TableSessionInvoice(
    val tableId: Int,
    val tableNumber: Int,
    val sessionId: String,
    val startTime: String,
    val orders: List<Order>,
    val subtotal: Double,
    val tax: Double,
    val total: Double,
    val paymentMethod: PaymentMethod? = null,
    val restaurantDetails: RestaurantDetails
)

@Serializable
data class RestaurantDetails(
    val name: String = "Your Restaurant Name",
    val address: String = "Restaurant Address",
    val phone: String = "Phone Number",
    val email: String = "email@restaurant.com",
    val taxNumber: String = "TAX123456",
    val footer: String = "Thank you for dining with us!"
) 