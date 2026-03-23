plugins {
    kotlin("jvm")
    `maven-publish`
    `java-library`
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

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("YuChat Bot API SDK — Java API")
                description.set("Java-friendly wrappers (CompletableFuture) for YuChat Bot API SDK")
                url.set("https://api.gitflame.rt.ru/ivan.patakin/yuchatbotapi.git")
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
            }
        }
    }
}
