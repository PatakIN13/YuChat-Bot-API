plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("ru.rt.yuchatbotapi.examples.WebhookBotKt")
}

dependencies {
    implementation(project(":yuchatbotapi-core"))
    implementation(project(":yuchatbotapi-webhook"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-simple:2.0.9")
}
