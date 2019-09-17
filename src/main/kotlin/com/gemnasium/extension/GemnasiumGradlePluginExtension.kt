package com.gemnasium.extension

import org.gradle.api.Project
import java.io.File

open class GemnasiumGradlePluginExtension (project : Project) {

    companion object {
        const val PLUGIN_EXTENSION_NAME = "gemnasiumGradlePlugin"
    }

    /**
     * The output directory into which the generated JSON dependency file is put.
     * The default value is <project_build_dir>/reports/
     */
    var outputDir = File(project.buildDir, "reports")

    /**
     * The name of the generated JSON dependency file.
     * The default value is gradle-dependencies.json
     */
    var outputFileName = "gradle-dependencies.json"

    /**
     * When set to true configurations that are considered a test configuration will not be included in the analysis.
     * A configuration is considered a test configuration if and only if any of the following conditions holds:
     * <ul>
     *     <li>the name of the configuration or any of its parent configurations equals 'testCompile'</li>
     *     <li>the name of the configuration or any of its parent configurations equals 'androidTestCompile'</li>
     *     <li>the configuration name starts with 'test'</li>
     *     <li>the configuration name starts with 'androidTest'</li>
     * </ul>
     * The default value is true.
     */
    var skipTestGroups = true

    /**
     * Names of the configurations to scan.
     *
     * This is mutually exclusive with the skipConfigurations property.
     */
    var scanConfigurations = listOf<String>()

    /**
     * Names of the configurations to skip when scanning.
     *
     * This is mutually exclusive with the scanConfigurations property.
     */
    var skipConfigurations = listOf<String>()

    /**
     * Whether or not to skip the execution of the plugin tasks.
     */
    var skip = false
}