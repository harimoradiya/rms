import io.github.smiley4.ktorswaggerui.dsl.routing.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route

class HomeRoute {
    fun Route.homeRoute() {
        get("/", {
            tags = listOf("Home")
            summary = "RMS Home Page"
            description = "Restaurant Management System welcome page"
            response {
                HttpStatusCode.OK to {
                    description = "Welcome page HTML"
                    body<String> { description = "HTML content" }
                }
            }
        }) {
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Restaurant Management System API</title>
                    <style>
                        body {
                            font-family: 'Segoe UI', sans-serif;
                            margin: 0;
                            padding: 0;
                            background: linear-gradient(135deg, #1e3c72 0%, #2a5298 100%);
                            color: white;
                            line-height: 1.6;
                        }
                        .container {
                            max-width: 1200px;
                            margin: 0 auto;
                            padding: 2rem;
                            text-align: center;
                        }
                        h1 {
                            font-size: 3.5rem;
                            margin-bottom: 1rem;
                            text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                        }
                        .version {
                            font-size: 1.2rem;
                            margin-bottom: 2rem;
                            color: #a8c0ff;
                        }
                        .links {
                            display: flex;
                            justify-content: center;
                            gap: 1rem;
                            margin: 2rem 0;
                        }
                        .link-button {
                            padding: 0.8rem 1.5rem;
                            background: rgba(255,255,255,0.1);
                            border: 1px solid rgba(255,255,255,0.2);
                            border-radius: 5px;
                            color: white;
                            text-decoration: none;
                            transition: all 0.3s ease;
                        }
                        .link-button:hover {
                            background: rgba(255,255,255,0.2);
                            transform: translateY(-2px);
                        }
                        .features {
                            display: grid;
                            grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
                            gap: 1.5rem;
                            margin-top: 3rem;
                        }
                        .feature {
                            background: rgba(255,255,255,0.1);
                            padding: 1.5rem;
                            border-radius: 10px;
                            backdrop-filter: blur(10px);
                            transition: transform 0.3s ease;
                        }
                        .feature:hover {
                            transform: translateY(-5px);
                        }
                        .feature h3 {
                            color: #a8c0ff;
                            margin-bottom: 1rem;
                        }
                        .stats {
                            display: flex;
                            justify-content: center;
                            gap: 2rem;
                            margin: 3rem 0;
                        }
                        .stat {
                            text-align: center;
                        }
                        .stat-value {
                            font-size: 2rem;
                            font-weight: bold;
                            color: #a8c0ff;
                        }
                        .stat-label {
                            font-size: 0.9rem;
                            opacity: 0.8;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h1>🍽️ Restaurant Management System</h1>
                        <div class="version">API Version 1.0</div>
                        
                        <div class="stats">
                            <div class="stat">
                                <div class="stat-value">8+</div>
                                <div class="stat-label">Core Features</div>
                            </div>
                            <div class="stat">
                                <div class="stat-value">50+</div>
                                <div class="stat-label">API Endpoints</div>
                            </div>
                            <div class="stat">
                                <div class="stat-value">99.9%</div>
                                <div class="stat-label">Uptime</div>
                            </div>
                        </div>

                        <div class="links">
                            <a href="/swagger" class="link-button">API Documentation</a>
                            <a href="https://github.com/harimoradiya/rms" class="link-button">GitHub Repository</a>
                        </div>

                        <div class="features">
                            <div class="feature">
                                <h3>🪑 Table Management</h3>
                                <p>Real-time table tracking, reservations, and session management</p>
                            </div>
                            <div class="feature">
                                <h3>📝 Order Processing</h3>
                                <p>Efficient order management with real-time updates</p>
                            </div>
                            <div class="feature">
                                <h3>👨‍🍳 Kitchen Display</h3>
                                <p>Streamlined kitchen operations and order tracking</p>
                            </div>
                            <div class="feature">
                                <h3>💳 Payment System</h3>
                                <p>Secure payments with multiple payment methods</p>
                            </div>
                            <div class="feature">
                                <h3>📊 Analytics</h3>
                                <p>Comprehensive reporting and business insights</p>
                            </div>
                            <div class="feature">
                                <h3>📦 Inventory</h3>
                                <p>Real-time stock management and alerts</p>
                            </div>
                            <div class="feature">
                                <h3>🔒 Security</h3>
                                <p>Role-based access control and JWT authentication</p>
                            </div>
                            <div class="feature">
                                <h3>📝 Feedback</h3>
                                <p>Customer feedback and rating system</p>
                            </div>
                        </div>
                    </div>
                </body>
                </html>
            """.trimIndent()
            
            call.respondText(html, ContentType.Text.Html)
        }
    }
} 