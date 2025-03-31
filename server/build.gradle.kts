plugins {
    java
    application
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

var main = "org.galliumpowered.Main"
var runDir = file("./run/")

runDir.mkdirs()

val libVersion: String by project
val minecraftVersion: String by project

dependencies {
    implementation("org.jetbrains:annotations:24.0.0")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.google.code.gson:gson:2.8.6")

    // Log4J
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.19.0")
    implementation("org.slf4j:slf4j-api:2.0.1")

    // Minecraft depends
    implementation("net.sf.jopt-simple:jopt-simple:5.0.4")
    implementation("io.netty:netty-all:4.1.10.Final")
    implementation("org.joml:joml:1.10.5")

    // Gallium
    implementation("net.kyori:adventure-api:4.2.0")
    implementation("net.kyori:event-method-asm:3.0.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.2.0")
    implementation("org.xerial:sqlite-jdbc:3.42.0.0")
    implementation("org.json:json:20230618")
    implementation("com.github.oshi:oshi-core:6.2.2")
    implementation("commons-io:commons-io:2.11.0")
    implementation("net.java.dev.jna:jna:5.12.1")
    implementation("com.google.guava:guava:32.0.1-jre")
    implementation("com.google.inject:guice:7.0.0")
    implementation("org.jline:jline:3.12.1")
    implementation("net.minecrell:terminalconsoleappender:1.3.0")

    // GalliumLib
    implementation(project(":lib"))

    // Test plugin
    implementation(project(":testplugin"))

    // TODO: Mixin, don't use this
    // ALSO TODO: 1.20
    implementation("net.minecraft:server:$minecraftVersion")
}

tasks {
    shadowJar {
        archiveBaseName.set("Gallium-$minecraftVersion-$libVersion")
    }
}

artifacts {
    archives(tasks.shadowJar)
}

application {
    mainClass.set(main)
}

tasks.named<JavaExec>("run") {
    workingDir = runDir
    args = listOf("--testplugin", "--nogui")
    standardInput = System.`in`
}