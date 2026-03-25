plugins {
    kotlin("jvm") version "1.9.22" apply false
    kotlin("plugin.serialization") version "1.9.22" apply false
    id("org.jetbrains.dokka") version "1.9.10"
    id("com.vanniktech.maven.publish") version "0.28.0" apply false
}

allprojects {
    group = "space.patakin.yuchatbotapi"
    version = "0.2.0"

    repositories {
        mavenCentral()
    }
}

subprojects {
    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Ivan Patakin",
                "Built-By" to System.getProperty("user.name"),
                "Build-Jdk" to System.getProperty("java.version"),
            )
        }
    }
}
