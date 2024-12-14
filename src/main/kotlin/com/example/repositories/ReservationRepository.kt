package com.example.repositories

import com.example.database.DatabaseFactory.dbQuery
import com.example.database.ReservationTable
import com.example.models.*
import org.jetbrains.exposed.sql.*

class ReservationRepository {
    
    suspend fun createReservation(reservation: Reservation): Reservation = dbQuery {
        val insertStatement = ReservationTable.insert {
            it[customerName] = reservation.customerName
            it[customerEmail] = reservation.customerEmail
            it[customerPhone] = reservation.customerPhone
            it[numberOfGuests] = reservation.numberOfGuests
            it[reservationDate] = reservation.reservationDate
            it[reservationTime] = reservation.reservationTime
            it[status] = reservation.status
            it[specialRequests] = reservation.specialRequests
        }
        
        val resultRow = insertStatement.resultedValues?.first()
        resultRow?.let {
            Reservation(
                id = it[ReservationTable.id],
                customerName = it[ReservationTable.customerName],
                customerEmail = it[ReservationTable.customerEmail],
                customerPhone = it[ReservationTable.customerPhone],
                numberOfGuests = it[ReservationTable.numberOfGuests],
                reservationDate = it[ReservationTable.reservationDate],
                reservationTime = it[ReservationTable.reservationTime],
                status = it[ReservationTable.status],
                specialRequests = it[ReservationTable.specialRequests]
            )
        } ?: throw Exception("Failed to create reservation")
    }

    suspend fun getAllReservations(): List<Reservation> = dbQuery {
        ReservationTable.selectAll().map { row ->
            Reservation(
                id = row[ReservationTable.id],
                customerName = row[ReservationTable.customerName],
                customerEmail = row[ReservationTable.customerEmail],
                customerPhone = row[ReservationTable.customerPhone],
                numberOfGuests = row[ReservationTable.numberOfGuests],
                reservationDate = row[ReservationTable.reservationDate],
                reservationTime = row[ReservationTable.reservationTime],
                status = row[ReservationTable.status],
                specialRequests = row[ReservationTable.specialRequests]
            )
        }
    }
} 