plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("ru.rt.yuchatbotapi.examples.FileUploadBotKt")
}

dependencies {
    implementation(project(":yuchatbotapi-core"))
    implementation(project(":yuchatbotapi-polling"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
