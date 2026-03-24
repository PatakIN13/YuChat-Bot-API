plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

val ktorVersion = "2.3.7"

dependencies {
    api(project(":core"))
    api(project(":polling"))
    api(project(":webhook"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-cio:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()

    pom {
        name.set("YuChat Bot API SDK — Java API")
        description.set("Java-friendly wrappers (CompletableFuture) for YuChat Bot API SDK")
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
