plugins {
    java
    `maven-publish`
}

val libVersion: String by project
val adventureVersion: String by project

group = "org.galliumpowered"
version = libVersion

dependencies {
    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("net.kyori:adventure-text-serializer-gson:${adventureVersion}")
    implementation("net.kyori:adventure-text-serializer-ansi:${adventureVersion}")
    implementation("net.kyori:adventure-text-serializer-plain:${adventureVersion}")
    implementation("net.kyori:event-method-asm:3.0.0")
    implementation("org.json:json:20230618")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.inject:guice:7.0.0")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "gallium"

            // Attach the API JAR
            artifact(tasks["jar"]) {
                classifier = ""
            }

            pom {
                name.set("Gallium")
                description.set("A Minecraft server API")
                url.set("https://galliumpowered.org")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/GalliumPowered/Gallium.git")
                    developerConnection.set("scm:git:ssh://github.com/GalliumPowered/Gallium.git")
                    url.set("https://github.com/GalliumPowered/Gallium")
                }
            }
        }
    }

    repositories {
        maven {
            url = uri("${System.getProperty("user.home")}/.m2/repository")
        }
    }
}