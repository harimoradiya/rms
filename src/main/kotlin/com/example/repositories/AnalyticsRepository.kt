package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.*
import com.example.models.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.between
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class AnalyticsRepository {
    
    private fun parseDateTime(dateStr: String): LocalDateTime {
        return LocalDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME)
    }

    suspend fun getSalesSummary(startDate: String, endDate: String): SalesSummary = dbQuery {
        val orders = OrderTable
            .select {
                OrderTable.createdAt.between(startDate, endDate)
            }
            .toList()

        val totalSales = orders.sumOf { it[OrderTable.totalAmount] }
        val totalOrders = orders.size
        val averageOrderValue = if (totalOrders > 0) totalSales / totalOrders else 0.0

        SalesSummary(
            totalSales = totalSales,
            totalOrders = totalOrders,
            averageOrderValue = averageOrderValue,
            period = "Custom"
        )
    }

    suspend fun getTopSellingItems(startDate: String, endDate: String, limit: Int = 10): List<ItemSalesReport> = dbQuery {
        (OrderItemTable innerJoin OrderTable innerJoin MenuItemTable)
            .slice(
                MenuItemTable.id,
                MenuItemTable.name,
                OrderItemTable.quantity.sum(),
                OrderItemTable.itemPrice.sum(),
                OrderItemTable.quantity.avg()
            )
            .select {
                OrderTable.createdAt.between(startDate, endDate)
            }
            .groupBy(MenuItemTable.id, MenuItemTable.name)
            .orderBy(OrderItemTable.quantity.sum() to SortOrder.DESC)
            .limit(limit)
            .map {
                ItemSalesReport(
                    menuItemId = it[MenuItemTable.id],
                    menuItemName = it[MenuItemTable.name],
                    quantitySold = it[OrderItemTable.quantity.sum()]?.toInt() ?: 0,
                    totalRevenue = it[OrderItemTable.itemPrice.sum()]?.toDouble() ?: 0.0,
                    averageOrderQuantity = it[OrderItemTable.quantity.avg()]?.toDouble() ?: 0.0
                )
            }
    }

    suspend fun getPaymentMethodBreakdown(startDate: String, endDate: String): List<PaymentMethodSummary> = dbQuery {
        val totalPayments = PaymentTable
            .select { PaymentTable.createdAt.between(startDate, endDate) }
            .sumOf { it[PaymentTable.amount] }

        PaymentTable
            .slice(
                PaymentTable.paymentMethod,
                PaymentTable.amount.sum(),
                PaymentTable.id.count()
            )
            .select { PaymentTable.createdAt.between(startDate, endDate) }
            .groupBy(PaymentTable.paymentMethod)
            .map {
                val methodTotal = it[PaymentTable.amount.sum()]?.toDouble() ?: 0.0
                PaymentMethodSummary(
                    paymentMethod = it[PaymentTable.paymentMethod],
                    totalAmount = methodTotal,
                    numberOfTransactions = it[PaymentTable.id.count()].toInt(),
                    percentageOfTotal = if (totalPayments > 0) (methodTotal / totalPayments) * 100 else 0.0
                )
            }
    }

    suspend fun getHourlyAnalytics(startDate: String, endDate: String): List<TimeBasedAnalytics> = dbQuery {
        // This is a simplified version. In production, you'd want to use proper datetime functions
        // specific to your database (PostgreSQL in this case)
        OrderTable
            .slice(
                OrderTable.createdAt,
                OrderTable.id.count(),
                OrderTable.totalAmount.sum()
            )
            .select { OrderTable.createdAt.between(startDate, endDate) }
            .groupBy(OrderTable.createdAt)
            .map {
                val hour = parseDateTime(it[OrderTable.createdAt]).hour
                TimeBasedAnalytics(
                    hour = hour,
                    numberOfOrders = it[OrderTable.id.count()].toInt(),
                    totalSales = it[OrderTable.totalAmount.sum()]?.toDouble() ?: 0.0
                )
            }
    }

    suspend fun getDailyAnalytics(startDate: String, endDate: String): List<TimeBasedAnalytics> = dbQuery {
        OrderTable
            .slice(
                OrderTable.createdAt,
                OrderTable.id.count(),
                OrderTable.totalAmount.sum()
            )
            .select { OrderTable.createdAt.between(startDate, endDate) }
            .groupBy(OrderTable.createdAt)
            .map {
                val dayOfWeek = parseDateTime(it[OrderTable.createdAt]).dayOfWeek.toString()
                TimeBasedAnalytics(
                    dayOfWeek = dayOfWeek,
                    numberOfOrders = it[OrderTable.id.count()].toInt(),
                    totalSales = it[OrderTable.totalAmount.sum()]?.toDouble() ?: 0.0
                )
            }
    }

    suspend fun getCompleteDashboard(startDate: String, endDate: String): SalesAnalyticsDashboard = dbQuery {
        SalesAnalyticsDashboard(
            salesSummary = getSalesSummary(startDate, endDate),
            topSellingItems = getTopSellingItems(startDate, endDate),
            paymentMethodBreakdown = getPaymentMethodBreakdown(startDate, endDate),
            peakHours = getHourlyAnalytics(startDate, endDate),
            revenueByDay = getDailyAnalytics(startDate, endDate)
        )
    }
} 