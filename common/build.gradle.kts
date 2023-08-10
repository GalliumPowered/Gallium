plugins {
    id("java")
}

group = "org.galliumpowered"
version = "1.17.1-1.1.0-beta.5"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
}

tasks.test {
    useJUnitPlatform()
}