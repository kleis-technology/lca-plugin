plugins {
    id("org.jetbrains.kotlin.jvm") version "1.7.20"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.commons:commons-csv:1.9.0")
    implementation("net.lingala.zip4j:zip4j:2.11.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("io.mockk:mockk:1.13.4")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
