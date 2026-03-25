buildscript {
    repositories {
        maven {
            name = "rt-safe-maven-central"
            url = uri("https://repository.rt.ru/repository/maven-repo.maven.apache.org")
        }
    }
    // Аналог <dependencyManagement> из Maven pom.xml —
    // принудительная замена версий, недоступных в Феникс
    configurations.classpath {
        resolutionStrategy {
            force(
                "com.fasterxml.jackson.core:jackson-core:2.21.1",
                "com.fasterxml.woodstox:woodstox-core:7.1.0",
                "org.jetbrains.kotlin:kotlin-compiler-embeddable:1.9.23"
            )
        }
    }
}

plugins {
    kotlin("jvm") version "1.9.24" apply false
    kotlin("plugin.serialization") version "1.9.24" apply false
    id("org.jetbrains.dokka") version "1.9.20" apply false
}

allprojects {
    group = "ru.rt.yuchatbotapi"
    version = "0.2.0"

    repositories {
        maven {
            name = "rt-safe-maven-central"
            url = uri("https://repository.rt.ru/repository/maven-repo.maven.apache.org")
        }
    }

    // Принудительная замена версий Jackson/Woodstox, недоступных в корп. репозитории (403),
    // во всех конфигурациях (включая Dokka)
    configurations.all {
        resolutionStrategy {
            force(
                "com.fasterxml.jackson.core:jackson-core:2.21.1",
                "com.fasterxml.jackson.core:jackson-databind:2.21.1",
                "com.fasterxml.jackson.core:jackson-annotations:2.21.1",
                "com.fasterxml.jackson.module:jackson-module-kotlin:2.21.1",
                "com.fasterxml.jackson.core:jackson-annotations:2.21",
                "com.fasterxml.woodstox:woodstox-core:7.1.0",
            )
        }
    }
}

subprojects {
    tasks.withType<Jar> {
        manifest {
            attributes(
                "Implementation-Title" to project.name,
                "Implementation-Version" to project.version,
                "Implementation-Vendor" to "Rostelecom",
                "Built-By" to System.getProperty("user.name"),
                "Build-Jdk" to System.getProperty("java.version"),
            )
        }
    }
}
