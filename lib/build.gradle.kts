plugins {
    java
}

val libVersion: String by project

group = "org.galliumpowered"
version = libVersion

apply(from = "../gradle/build.gradle")

dependencies {
    compileOnly("com.mojang:brigadier:1.0.18")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.2.0")
    implementation("net.kyori:event-method-asm:3.0.0")
    implementation("org.json:json:20230618")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.inject:guice:7.0.0")
}