plugins {
    java
    application
}

dependencies {
    implementation(project(":java-api"))
}

application {
    mainClass.set("ru.rt.yuchatbotapi.examples.EchoBotJava")
}
