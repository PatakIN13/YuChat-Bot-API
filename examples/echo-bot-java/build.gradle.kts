plugins {
    java
    application
}

dependencies {
    implementation(project(":yuchatbotapi-java"))
}

application {
    mainClass.set("ru.rt.yuchatbotapi.examples.EchoBotJava")
}
