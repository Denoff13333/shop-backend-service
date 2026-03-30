plugins {
    kotlin("plugin.serialization")
    application
}

application {
    mainClass.set("com.example.shop.worker.WorkerMainKt")
}

dependencies {
    implementation(project(":shared"))
    implementation("com.rabbitmq:amqp-client:5.24.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("ch.qos.logback:logback-classic:1.5.18")
}
