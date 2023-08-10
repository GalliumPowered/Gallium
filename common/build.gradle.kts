plugins {
    id("java")
}

val libVersion: String by project

group = "org.galliumpowered"
version = "$libVersion"

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