/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.gemnasium

import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import org.gradle.testkit.runner.GradleRunner
import kotlin.io.path.toPath
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertContains
import kotlin.test.assertEquals

/**
 * A simple functional test for the 'com.gemnasium.greeting' plugin.
 */
class GemnasiumGradlePluginFunctionalTest {
    @Test fun `can run task`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val outputFileNameValue = "deps.json"
        projectDir.resolve("settings.gradle").writeText("")
        val buildFile = projectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id('com.gemnasium.gradle-plugin')
                id('java')
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation group: 'org.aeonbits.owner', name: 'owner', version:'1.0.10'
            }

            gemnasiumGradlePlugin {
                outputFileName = '${outputFileNameValue}'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("gemnasiumDumpDependencies")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        val outputFile = File(projectDir, "build/reports/${outputFileNameValue}")

        assertContains(result.output, "Writing dependency JSON to")
        assertTrue(outputFile.exists())

        // Verify that output file contains valid JSON content
        val parser = ObjectMapper().factory.createParser(outputFile)
        while (parser.nextToken() != null) {}

        // Verify that the dependency we had in our gradle project is in the output file
        assertContains(outputFile.readText(), "org.aeonbits.owner")
    }

    @Test fun `empty project does not produce output file`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val outputFileNameValue = "deps.json"
        projectDir.resolve("settings.gradle").writeText("")
        val buildFile = projectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id('com.gemnasium.gradle-plugin')
                id('java')
            }

            repositories {
                mavenCentral()
            }

            gemnasiumGradlePlugin {
                outputFileName = '${outputFileNameValue}'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("gemnasiumDumpDependencies")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        val outputFile = File(projectDir, "build/reports/${outputFileNameValue}")

        assertContains(result.output, "No dependencies found in project")
        assertTrue(!outputFile.exists())
    }


    @Test fun `invalid dependency project exits with an error`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val outputFileNameValue = "deps.json"
        projectDir.resolve("settings.gradle").writeText("")
        val buildFile = projectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id('com.gemnasium.gradle-plugin')
                id('java')
            }

            repositories {
                maven { url "https://example.com" }
            }

            dependencies {
                implementation group: 'fluff', name: 'invalid', version:'1.0.10'
                implementation group: 'fuzz', name: 'broken', version:'2.1.20'
            }

            gemnasiumGradlePlugin {
                outputFileName = '${outputFileNameValue}'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("gemnasiumDumpDependencies")
        runner.withProjectDir(projectDir)

        val result = runner.buildAndFail()

        assertContains(result.output, "Project has 2 unresolved dependencies: fluff:invalid:1.0.10, fuzz:broken:2.1.20")
    }

    @Test fun `handles nested dependencies`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val outputFileNameValue = "deps.json"
        projectDir.resolve("settings.gradle").writeText("")
        val buildFile = projectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id('com.gemnasium.gradle-plugin')
                id('java')
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation 'org.slf4j:slf4j-api:1.7.30'
                implementation "org.apache.logging.log4j:log4j-api:2.13.2"
                implementation "org.apache.logging.log4j:log4j-slf4j-impl:2.13.2"
            }

            gemnasiumGradlePlugin {
                outputFileName = '${outputFileNameValue}'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("gemnasiumDumpDependencies")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        val outputFile = File(projectDir, "build/reports/${outputFileNameValue}")
        val fixtureFile = this::class.java.getResource("/nested-dependencies/deps.json").readText()

        assertTrue(result.output.contains("Writing dependency JSON to"))
        assertTrue(outputFile.exists())

        // Verify that output file contains valid JSON content
        val parser = ObjectMapper().factory.createParser(outputFile)
        while (parser.nextToken() != null) {}

        // Verify that the dependency we had in our gradle project is in the output file
        assertEquals(fixtureFile, outputFile.readText())
    }

    @Test fun `handles circular dependencies`() {
        // Setup the test build
        val projectDir = File("build/functionalTest")
        projectDir.deleteRecursively()
        projectDir.mkdirs()

        val outputFileNameValue = "deps.json"
        projectDir.resolve("settings.gradle").writeText("")
        val buildFile = projectDir.resolve("build.gradle")
        buildFile.writeText("""
            plugins {
                id('com.gemnasium.gradle-plugin')
                id('java')
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation 'org.codehaus.groovy:groovy-all:3.0.8'
            }

            gemnasiumGradlePlugin {
                outputFileName = '${outputFileNameValue}'
            }
        """)

        // Run the build
        val runner = GradleRunner.create()
        runner.forwardOutput()
        runner.withPluginClasspath()
        runner.withArguments("gemnasiumDumpDependencies")
        runner.withProjectDir(projectDir)
        val result = runner.build()

        // Verify the result
        val outputFile = File(projectDir, "build/reports/${outputFileNameValue}")
        val fixtureFile = this::class.java.getResource("/circular-dependencies/deps.json").readText()

        assertTrue(result.output.contains("Writing dependency JSON to"))
        assertTrue(outputFile.exists())

        // Verify that output file contains valid JSON content
        val parser = ObjectMapper().factory.createParser(outputFile)
        while (parser.nextToken() != null) {}

        // Verify that the dependency we had in our gradle project is in the output file
        assertEquals(fixtureFile, outputFile.readText())
    }
}
