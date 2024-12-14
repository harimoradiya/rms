val h2_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val postgres_version: String by project
val swagger_version: String by project

plugins {
    kotlin("jvm") version "2.0.21"
    id("io.ktor.plugin") version "3.0.1"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}



group = "com.example"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")


}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
//    maven {
//        url = uri("https://mvnrepository.com/artifact/com.itextpdf/itext7-core")
//    }
}


dependencies {
    implementation("io.github.smiley4:ktor-swagger-ui:4.0.0")
    implementation("io.ktor:ktor-server-auth-jvm")
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-auth-jwt-jvm")
    implementation("io.ktor:ktor-server-status-pages")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm")
    implementation("io.ktor:ktor-server-content-negotiation-jvm")
    implementation("org.postgresql:postgresql:$postgres_version")
    implementation("com.h2database:h2:$h2_version")
    implementation("io.ktor:ktor-server-swagger:3.0.1")
    implementation("org.jetbrains.exposed:exposed-core:0.53.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.53.0")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:0.41.1")

    // PostgreSQL Driver
    implementation("org.postgresql:postgresql:42.6.0")

    // Connection Pooling
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("io.ktor:ktor-server-config-yaml")
    testImplementation("io.ktor:ktor-server-test-host-jvm")
//    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlin_version")
    implementation("org.mindrot:jbcrypt:0.4")
    // Add these dependencies for PDF generation
    val iTextVersion = "9.0.0"
    implementation("com.itextpdf:itext-core:$iTextVersion")
    implementation("com.itextpdf:kernel:$iTextVersion")
    implementation("com.itextpdf:layout:$iTextVersion")
    implementation("org.apache.pdfbox:pdfbox:2.0.27")
}
