plugins {
    kotlin("jvm")
    application
}

application {
    mainClass.set("ru.rt.yuchatbotapi.examples.FileUploadBotKt")
}

dependencies {
    implementation(project(":core"))
    implementation(project(":polling"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
