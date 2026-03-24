pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "yuchatbotapi"

include("core")
include("polling")
include("webhook")
include("java-api")
include("examples:echo-bot")
include("examples:echo-bot-java")
include("examples:file-upload-bot")
include("examples:test-all-bot")
