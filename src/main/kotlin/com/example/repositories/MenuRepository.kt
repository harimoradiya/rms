package com.example.repositories

import com.example.database.DatabaseFactory
import com.example.database.MenuItemTable
import com.example.models.MenuItem
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class MenuRepository {

    suspend fun create(menuItem: MenuItem) : MenuItem = DatabaseFactory.dbQuery {
        val insertId = MenuItemTable.insert {
            it[name]= menuItem.name
            it[description] = menuItem.description
            it[price] = menuItem.price.toBigDecimal()
            it[category] = menuItem.category
            it[isAvailable] = menuItem.isAvailable
        } get MenuItemTable.id
        menuItem.copy(id = insertId)

    }

    suspend fun getAll(): List<MenuItem> = DatabaseFactory.dbQuery {
        MenuItemTable.selectAll().map { row ->
            MenuItem(
                id = row[MenuItemTable.id],
                name = row[MenuItemTable.name],
                description = row[MenuItemTable.description],
                price = row[MenuItemTable.price].toString(),
                category = row[MenuItemTable.category],
                isAvailable = row[MenuItemTable.isAvailable]
            )
        }
    }

    suspend fun getById(id: Int): MenuItem? = DatabaseFactory.dbQuery {
        MenuItemTable.select { MenuItemTable.id eq id }
            .map { row ->
                MenuItem(
                    id = row[MenuItemTable.id],
                    name = row[MenuItemTable.name],
                    description = row[MenuItemTable.description],
                    price = row[MenuItemTable.price].toString(),
                    category = row[MenuItemTable.category],
                    isAvailable = row[MenuItemTable.isAvailable]
                )
            }.singleOrNull()
    }

    suspend fun getAvailableItem(isAvailable: Boolean): List<MenuItem> = DatabaseFactory.dbQuery {
        MenuItemTable.select { MenuItemTable.isAvailable eq isAvailable }.map { row ->
            println("My items - ${row.toString()}")
            MenuItem(
                id = row[MenuItemTable.id],
                name = row[MenuItemTable.name],
                description = row[MenuItemTable.description],
                price = row[MenuItemTable.price].toString(),
                category = row[MenuItemTable.category],
                isAvailable = row[MenuItemTable.isAvailable]
            )
        }
    }

    suspend fun update(id: Int, menuItem: MenuItem): MenuItem? = DatabaseFactory.dbQuery {
        val updatedRows = MenuItemTable.update({ MenuItemTable.id eq id }) {
            it[name] = menuItem.name
            it[description] = menuItem.description
            it[price] = menuItem.price.toBigDecimal()
            it[category] = menuItem.category
            it[isAvailable] = menuItem.isAvailable
        }

        if (updatedRows > 0) {
            menuItem.copy(id = id)
        } else {
            null
        }
    }


    suspend fun delete(id: Int): Boolean = DatabaseFactory.dbQuery {
        MenuItemTable.deleteWhere { MenuItemTable.id eq id } > 0
    }


}