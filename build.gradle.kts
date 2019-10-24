/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Gradle plugin project to get you started.
 * For more details take a look at the Writing Custom Plugins chapter in the Gradle
 * User Manual available at https://docs.gradle.org/5.5.1/userguide/custom_plugins.html
 */

group = "com.gemnasium"
version = "0.2"

plugins {
    kotlin("jvm") version "1.3.50"
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "0.10.1"
    id("maven-publish")

    id("jacoco")
    id("com.github.ksoichiro.console.reporter") version "0.6.2"
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.9.2")

    // Use the Kotlin test library.
    testImplementation(kotlin("test"))

    // Use the Kotlin JUnit integration.
    testImplementation(kotlin("test-junit"))

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.0")
}

fun checkAndSetProperty( prop: String, value: String? ){
    if (value != null) {
        project.extra[prop] = value
    }
}

// Grab the gradle publish key and secret from environment variables if they're set.
checkAndSetProperty("gradle.publish.key", System.getenv("GRADLE_PUBLISH_KEY"))
checkAndSetProperty("gradle.publish.secret", System.getenv("GRADLE_PUBLISH_SECRET"))

gradlePlugin {
    plugins {
        create("gemnasium-gradle-plugin") {
            id = "com.gemnasium.gradle-plugin"
            implementationClass = "com.gemnasium.GemnasiumGradlePlugin"
            displayName = "Gemnasium Gradle Plugin"
            description = "A Gradle plugin to produce a dependency report in JSON format that Gemnasium can use for a dependency vulnerability scan"
        }
    }
}

pluginBundle {
    website = "https://gitlab.com/stfs/gemnasium-gradle-plugin"
    vcsUrl = "https://gitlab.com/stfs/gemnasium-gradle-plugin.git"
    tags = listOf("gemnasium", "dependency", "dependencies", "dependency-check", "security", "gitlab")
}

// Add a source set for the functional test suite
val functionalTestSourceSet = sourceSets.create("functionalTest") {
}

gradlePlugin.testSourceSets(functionalTestSourceSet)
configurations.getByName("functionalTestImplementation").extendsFrom(configurations.getByName("testImplementation"))

// Add a task to run the functional tests
val functionalTest by tasks.creating(Test::class) {
    testClassesDirs = functionalTestSourceSet.output.classesDirs
    classpath = functionalTestSourceSet.runtimeClasspath
}

val check by tasks.getting(Task::class) {
    // Run the functional tests as part of `check`
    dependsOn(functionalTest)
}

tasks.named("reportCoverage") {
    dependsOn("jacocoTestReport")
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = "com.gemnasium"
            artifactId = "gradle-plugin"

            //from(components["java"])
        }
    }
}