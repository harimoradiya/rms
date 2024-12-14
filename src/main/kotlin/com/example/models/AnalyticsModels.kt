package com.example.models

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class SalesSummary(
    val totalSales: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val period: String // Daily, Weekly, Monthly, Yearly
)

@Serializable
data class ItemSalesReport(
    val menuItemId: Int,
    val menuItemName: String,
    val quantitySold: Int,
    val totalRevenue: Double,
    val averageOrderQuantity: Double
)

@Serializable
data class PaymentMethodSummary(
    val paymentMethod: PaymentMethod,
    val totalAmount: Double,
    val numberOfTransactions: Int,
    val percentageOfTotal: Double
)

@Serializable
data class TimeBasedAnalytics(
    val hour: Int? = null,
    val dayOfWeek: String? = null,
    val numberOfOrders: Int,
    val totalSales: Double
)

@Serializable
data class AnalyticsRequest(
    val startDate: String,
    val endDate: String,
    val groupBy: String? = null // hourly, daily, weekly, monthly
)

@Serializable
data class SalesAnalyticsDashboard(
    val salesSummary: SalesSummary,
    val topSellingItems: List<ItemSalesReport>,
    val paymentMethodBreakdown: List<PaymentMethodSummary>,
    val peakHours: List<TimeBasedAnalytics>,
    val revenueByDay: List<TimeBasedAnalytics>
) 