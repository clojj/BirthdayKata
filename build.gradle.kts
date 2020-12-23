import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30-M1"
    // id("org.jetbrains.kotlin.jvm") version "1.4.30-M1"
}

group = "com.clojj"
version = "1.0-SNAPSHOT"

repositories {
    maven("https://oss.jfrog.org/artifactory/oss-snapshot-local/")
    mavenCentral()
    jcenter()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))

    val arrow_version = "latest.integration"
    implementation("io.arrow-kt:arrow-fx:$arrow_version")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrow_version")
    // implementation("io.arrow-kt:arrow-fx-stm:0.12.0-SNAPSHOT")

    val ktor_version = "1.4.2"
    implementation("io.ktor:ktor-client-cio:$ktor_version")

    implementation("com.sun.mail:javax.mail:latest.release")
    implementation("io.github.microutils:kotlin-logging:latest.release")
    implementation("org.slf4j:slf4j-simple:latest.release")

    val kotest = "latest.release"
    testImplementation("io.kotest:kotest-runner-junit5:$kotest") // for kotest framework
    testImplementation("io.kotest:kotest-assertions-core:$kotest") // for kotest core jvm assertions
    testImplementation("com.github.kirviq:dumbster:1.7.1")
}

tasks {
    compileKotlin {
        kotlinOptions.apply {
            jvmTarget = "11"
            // freeCompilerArgs = freeCompilerArgs + "-Xallow-result-return-type"
        }
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "11"
    }
}
val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    freeCompilerArgs = listOf("-Xinline-classes")
}
