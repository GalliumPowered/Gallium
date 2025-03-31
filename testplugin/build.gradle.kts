plugins {
    id("java")
}

group = "org.galliumpowered"
version = "1.0"

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")

    implementation(project(":lib"))

    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
    implementation("org.slf4j:slf4j-api:2.0.1")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.inject:guice:7.0.0")
    implementation("net.kyori:adventure-api:4.2.0")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}