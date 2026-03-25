plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jetbrains.dokka")
    `maven-publish`
    `java-library`
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

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
            pom {
                name.set("YuChat Bot API SDK — Polling")
                description.set("Long-polling module for YuChat Bot API SDK")
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
