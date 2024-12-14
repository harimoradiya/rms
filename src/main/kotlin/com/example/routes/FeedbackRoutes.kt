package com.example.routes

import com.example.models.*
import com.example.repositories.FeedbackRepository
import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.github.smiley4.ktorswaggerui.dsl.routing.post
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

class FeedbackRoutes(private val feedbackRepository: FeedbackRepository) {

    fun Route.feedbackRoutes() {
        route("/feedback") {
            post("/submit", {
                tags = listOf("Feedback")
                summary = "Submit feedback"
                description = "Submit feedback for an order"
                request {
                    body<FeedbackRequest> {
                        description = "Feedback details"
                    }
                }
                response {
                    HttpStatusCode.Created to {
                        description = "Feedback submitted successfully"
                        body<Feedback> { description = "Created feedback details" }
                    }
                    HttpStatusCode.BadRequest to {
                        description = "Invalid feedback data"
                    }
                }
            }) {
                try {
                    val request = call.receive<FeedbackRequest>()
                    
                    // Validate rating
                    if (request.rating !in 1..5) {
                        call.respond(HttpStatusCode.BadRequest, "Rating must be between 1 and 5")
                        return@post
                    }

                    val feedback = Feedback(
                        orderId = request.orderId,
                        rating = request.rating,
                        comment = request.comment,
                        foodQuality = request.foodQuality,
                        serviceQuality = request.serviceQuality,
                        ambience = request.ambience,
                        cleanliness = request.cleanliness
                    )
                    
                    val createdFeedback = feedbackRepository.createFeedback(feedback)
                    call.respond(HttpStatusCode.Created, createdFeedback)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        mapOf("error" to (e.message ?: "Failed to submit feedback"))
                    )
                }
            }

            get("/summary", {
                tags = listOf("Feedback")
                summary = "Get feedback summary"
                description = "Get summary of all feedback including averages and distributions"
                response {
                    HttpStatusCode.OK to {
                        description = "Feedback summary retrieved successfully"
                        body<FeedbackSummary> { description = "Feedback summary" }
                    }
                }
            }) {
                try {
                    val summary = feedbackRepository.getFeedbackSummary()
                    call.respond(HttpStatusCode.OK, summary)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve feedback summary"))
                    )
                }
            }

            get("/recent", {
                tags = listOf("Feedback")
                summary = "Get recent feedback"
                description = "Get list of recent feedback"
                request {
                    queryParameter<Int>("limit") {
                        description = "Number of feedback entries to retrieve"
                        required = false
//                        defaultValue = 10
                    }
                }
                response {
                    HttpStatusCode.OK to {
                        description = "Recent feedback retrieved successfully"
                        body<List<Feedback>> { description = "List of recent feedback" }
                    }
                }
            }) {
                try {
                    val limit = call.parameters["limit"]?.toIntOrNull() ?: 10
                    val recentFeedback = feedbackRepository.getRecentFeedbacks(limit)
                    call.respond(HttpStatusCode.OK, recentFeedback)
                } catch (e: Exception) {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        mapOf("error" to (e.message ?: "Failed to retrieve recent feedback"))
                    )
                }
            }
        }
    }
} 