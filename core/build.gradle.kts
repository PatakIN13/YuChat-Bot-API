plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

val ktorVersion = "2.3.7"

dependencies {
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-client-logging:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("YuChat Bot API SDK — Core")
        description.set("Core module: HTTP client, API classes, and models for YuChat Bot API")
        url.set("https://github.com/patakin/yuchatbotapi")
        licenses {
            license {
                name.set("MIT License")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("ivan.patakin")
                name.set("Ivan Patakin")
            }
        }
        scm {
            connection.set("scm:git:git://github.com/patakin/yuchatbotapi.git")
            developerConnection.set("scm:git:ssh://github.com/patakin/yuchatbotapi.git")
            url.set("https://github.com/patakin/yuchatbotapi")
        }
    }
}
