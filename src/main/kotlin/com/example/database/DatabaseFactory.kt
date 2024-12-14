package com.example.database

import com.example.models.KitchenOrderStatus
import com.example.models.OrderStatus
import com.example.models.PaymentMethod
import com.example.models.PaymentStatus
import com.example.models.ReservationStatus
import com.example.models.StockStatus
import com.example.models.TableStatus
import com.example.models.TransactionType
import com.example.models.UnitType
import com.example.models.UserRole
import io.github.smiley4.ktorswaggerui.data.ref
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init(){
        val driverClassName  = "org.postgresql.Driver"
        val jdbcURL = "jdbc:postgresql://localhost:5432/restaurant_db"
        val user = "postgres"
        val password = "1234"

        Database.connect(
            url = jdbcURL,
            driver =  driverClassName,
            user = user,
            password =  password
        )

        transaction {
            SchemaUtils.create(
                RestaurantTable,
                MenuItemTable,
                OrderTable,
                OrderItemTable,
                UserTable,
                PaymentTable,
                ReservationTable,
                FeedbackTable
            )
        }
    }


    suspend fun <T> dbQuery (block : suspend  () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {block()}
//    suspend fun <T> dbQuery(block: suspend () -> T): T =
//        newSuspendedTransaction(Dispatchers.IO) { block() }
}



object RestaurantTable : Table("restaurant"){
    val id = integer("table_id").autoIncrement()
    val tableNumber = integer("table_number").uniqueIndex()
    val capacity = integer("capacity")
    val status = enumerationByName("status",50,TableStatus::class)
    override val primaryKey = PrimaryKey(id)
}

// Define database tables using Exposed ORM
object MenuItemTable : Table("menu_items") {
    val id = integer("menu_item_id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description")
    val price = decimal("price", 10, 2)
    val category = varchar("category", 50)
    val isAvailable = bool("is_available").default(true)
    override val primaryKey = PrimaryKey(id)
}


object OrderTable : Table("orders") {
    val id = integer("order_id").autoIncrement()
    val tableId = reference("table_id",RestaurantTable.id)
    val sessionId = varchar("session_id", 50)
    val status = enumerationByName("status", 50,OrderStatus::class)
    val totalAmount = double("total_amount")
    val createdAt = varchar("created_at", 50)
    override val primaryKey = PrimaryKey(id)
}

object OrderItemTable : Table("order_items") {
    val id = integer("id").autoIncrement()
    val orderId = reference("order_id", OrderTable.id)
    val menuItemId = reference("menu_item_id", MenuItemTable.id)
    val quantity = integer("quantity")
    val itemPrice = float("item_price")
    val specialInstructions = text("special_instructions").nullable()
    override val primaryKey = PrimaryKey(id)
}

object UserTable : Table("users") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 100).uniqueIndex()
    val password = varchar("password", 100)
    val role = enumerationByName("role", 20, UserRole::class)
    val isActive = bool("is_active").default(true)
    override val primaryKey = PrimaryKey(id)
}

object PaymentTable : Table("payments") {
    val id = integer("id").autoIncrement()
    val orderId = reference("order_id", OrderTable.id)
    val amount = double("amount")
    val paymentMethod = enumerationByName("payment_method", 20, PaymentMethod::class)
    val paymentStatus = enumerationByName("payment_status", 20, PaymentStatus::class)
    val transactionReference = varchar("transaction_reference", 100).nullable()
    val createdAt = varchar("created_at", 50)
    override val primaryKey = PrimaryKey(id)
}

object ReservationTable : Table("reservations") {
    val id = integer("id").autoIncrement()
    val customerName = varchar("customer_name", 100)
    val customerEmail = varchar("customer_email", 100)
    val customerPhone = varchar("customer_phone", 20)
    val numberOfGuests = integer("number_of_guests")
    val reservationDate = varchar("reservation_date", 20)
    val reservationTime = varchar("reservation_time", 20)
    val status = enumerationByName("status", 20, ReservationStatus::class)
    val specialRequests = text("special_requests").nullable()
    override val primaryKey = PrimaryKey(id)
}

object KitchenOrderTable : Table("kitchen_orders") {
    val id = integer("id").autoIncrement()
    val orderId = reference("order_id", OrderTable.id)
    val tableNumber = integer("table_number")
    val status = enumerationByName("status", 20, KitchenOrderStatus::class)
    val priority = integer("priority").default(1)
    val estimatedPrepTime = integer("estimated_prep_time").nullable()
    val startedAt = varchar("started_at", 50).nullable()
    val completedAt = varchar("completed_at", 50).nullable()
    override val primaryKey = PrimaryKey(id)
}

object KitchenOrderItemTable : Table("kitchen_order_items") {
    val id = integer("id").autoIncrement()
    val kitchenOrderId = reference("kitchen_order_id", KitchenOrderTable.id)
    val menuItemName = varchar("menu_item_name", 100)
    val quantity = integer("quantity")
    val specialInstructions = text("special_instructions").nullable()
    val status = enumerationByName("status", 20, KitchenOrderStatus::class)
    override val primaryKey = PrimaryKey(id)
}

object FeedbackTable : Table("feedbacks") {
    val id = integer("id").autoIncrement()
    val orderId = reference("order_id", OrderTable.id)
    val userId = reference("user_id", UserTable.id).nullable()
    val rating = integer("rating")
    val comment = text("comment").nullable()
    val foodQuality = integer("food_quality").nullable()
    val serviceQuality = integer("service_quality").nullable()
    val ambience = integer("ambience").nullable()
    val cleanliness = integer("cleanliness").nullable()
    val createdAt = varchar("created_at", 50)
    val isAnonymous = bool("is_anonymous").default(false)
    override val primaryKey = PrimaryKey(id)
}

object InventoryItemTable : Table("inventory_items") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", 100)
    val description = text("description").nullable()
    val quantity = double("quantity")
    val unitType = enumerationByName("unit_type", 20, UnitType::class)
    val minimumStockLevel = double("minimum_stock_level")
    val maximumStockLevel = double("maximum_stock_level")
    val status = enumerationByName("status", 20, StockStatus::class)
    val cost = double("cost")
    val supplier = varchar("supplier", 100).nullable()
    val location = varchar("location", 100).nullable()
    val lastRestockedAt = varchar("last_restocked_at", 50).nullable()
    override val primaryKey = PrimaryKey(id)
}

object InventoryTransactionTable : Table("inventory_transactions") {
    val id = integer("id").autoIncrement()
    val itemId = reference("item_id", InventoryItemTable.id)
    val type = enumerationByName("type", 20, TransactionType::class)
    val quantity = double("quantity")
    val previousQuantity = double("previous_quantity")
    val newQuantity = double("new_quantity")
    val reason = text("reason").nullable()
    val transactionDate = varchar("transaction_date", 50)
    val userId = reference("user_id", UserTable.id).nullable()
    override val primaryKey = PrimaryKey(id)
}