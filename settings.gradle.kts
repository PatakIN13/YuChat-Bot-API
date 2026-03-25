pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "yuchatbotapi"

include("core")
project(":core").name = "yuchatbotapi-core"
include("polling")
project(":polling").name = "yuchatbotapi-polling"
include("webhook")
project(":webhook").name = "yuchatbotapi-webhook"
include("java-api")
project(":java-api").name = "yuchatbotapi-java"
include("examples:echo-bot")
include("examples:echo-bot-java")
include("examples:file-upload-bot")
include("examples:test-all-bot")
