/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.gemnasium

import org.gradle.testfixtures.ProjectBuilder
import kotlin.test.Test
import kotlin.test.assertNotNull

/**
 * A simple unit test for the 'com.gemnasium.greeting' plugin.
 */
class GemnasiumGradlePluginKotlinPluginTest {
    @Test fun `plugin registers task`() {
        // Create a test project and apply the plugin
        val project = ProjectBuilder.builder().build()
        project.plugins.apply("com.gemnasium.gradle-plugin")

        // Verify the result
        assertNotNull(project.tasks.findByName("greeting"))
        assertNotNull(project.tasks.findByName("gemnasiumDumpDependencies"))
    }
}

