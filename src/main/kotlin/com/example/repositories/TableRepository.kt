package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.RestaurantTable
import com.example.models.Table
import com.example.models.TableStatus
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class TableRepository {
    
    suspend fun getAvailableTables(): List<Table> = dbQuery {
        RestaurantTable
            .select { RestaurantTable.status eq TableStatus.AVAILABLE }
            .map { row ->
                Table(
                    id = row[RestaurantTable.id],
                    tableNumber = row[RestaurantTable.tableNumber],
                    capacity = row[RestaurantTable.capacity],
                    status = row[RestaurantTable.status]
                )
            }
    }

    suspend fun updateTableStatus(id: Int, status: TableStatus): Table? = dbQuery {
        RestaurantTable.update({ RestaurantTable.id eq id }) {
            it[RestaurantTable.status] = status
        }

        RestaurantTable
            .select { RestaurantTable.id eq id }
            .map { row ->
                Table(
                    id = row[RestaurantTable.id],
                    tableNumber = row[RestaurantTable.tableNumber],
                    capacity = row[RestaurantTable.capacity],
                    status = row[RestaurantTable.status]
                )
            }
            .singleOrNull()
    }

    suspend fun getTableById(id: Int): Table? = dbQuery {
        RestaurantTable
            .select { RestaurantTable.id eq id }
            .map { row ->
                Table(
                    id = row[RestaurantTable.id],
                    tableNumber = row[RestaurantTable.tableNumber],
                    capacity = row[RestaurantTable.capacity],
                    status = row[RestaurantTable.status]
                )
            }
            .singleOrNull()
    }
} 