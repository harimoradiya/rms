package com.example.routes


import HomeRoute
import com.example.models.MenuItem
import com.example.models.OrderRequest
import com.example.repositories.MenuRepository
import com.example.repositories.OrderRepository
import com.example.repositories.UserRepository
import com.example.repositories.PaymentRepository
import com.example.repositories.ReservationRepository
import com.example.repositories.KitchenRepository
import com.example.repositories.FeedbackRepository
import com.example.repositories.InventoryRepository
import com.example.repositories.AnalyticsRepository
import com.example.repositories.TableRepository
import com.example.services.InvoiceService
import com.example.utils.PDFGenerator
//import com.example.utils.PDFGenerator
import com.example.services.UserService

import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.github.smiley4.ktorswaggerui.routing.openApiSpec
import io.github.smiley4.ktorswaggerui.routing.swaggerUI
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond

import io.ktor.server.routing.routing
import io.ktor.server.routing.route



fun Application.configureRouting() {


    routing {

        // add the routes for swagger-ui and api-spec
        route("swagger") {
            swaggerUI("/api.json")
        }
        route("api.json") {
            openApiSpec()
        }



        // Add HomeRoute
        val homeRoute = HomeRoute()
        with(homeRoute) {
            homeRoute()
        }

        val userRepository = UserRepository()
        val userService = UserService(userRepository)
        val authRoutes = AuthRoutes(userService)

        with(authRoutes) {
            authRoutes()
        }

        val tableRepository = TableRepository()
        val tableRoutes = TableRoutes(tableRepository)
        with(tableRoutes){
            tableRoutes()
        }

        // Create repository instance
        val menuRepository = MenuRepository()

        // Create routes instance
        val menuRoutes = MenuRoutes(menuRepository)

        with(menuRoutes) {
            menuRoutes()
        }

        val orderRepository = OrderRepository()
        val orderRoute = OrderRoutes(orderRepository)

        with(orderRoute){
            orderRoutes()
        }


        val paymentRepository = PaymentRepository()
        val paymentRoutes = PaymentRoutes(paymentRepository)

        with(paymentRoutes) {
            paymentRoutes()
        }

        val reservationRepository = ReservationRepository()
        val reservationRoutes = ReservationRoutes(reservationRepository)

        with(reservationRoutes) {
            reservationRoutes()
        }

        val kitchenRepository = KitchenRepository()
        val kitchenRoutes = KitchenRoutes(kitchenRepository)

        with(kitchenRoutes) {
            kitchenRoutes()
        }

        val feedbackRepository = FeedbackRepository()
        val feedbackRoutes = FeedbackRoutes(feedbackRepository)

        with(feedbackRoutes) {
            feedbackRoutes()
        }

        val inventoryRepository = InventoryRepository()
        val inventoryRoutes = InventoryRoutes(inventoryRepository)

        with(inventoryRoutes) {
            inventoryRoutes()
        }

        val analyticsRepository = AnalyticsRepository()
        val analyticsRoutes = AnalyticsRoutes(analyticsRepository)

        with(analyticsRoutes) {
            analyticsRoutes()
        }


        val invoiceService = InvoiceService(orderRepository,tableRepository, PDFGenerator())
        val invoiceRoutes = InvoiceRoutes(invoiceService)

        with(invoiceRoutes) {
            invoiceRoutes()
        }



    }

}


