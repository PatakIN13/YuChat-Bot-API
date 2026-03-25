plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    id("com.vanniktech.maven.publish")
}

dependencies {
    api(project(":yuchatbotapi-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.slf4j:slf4j-api:2.0.9")

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("YuChat Bot API SDK — Polling")
        description.set("Long-polling module for YuChat Bot API SDK")
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
