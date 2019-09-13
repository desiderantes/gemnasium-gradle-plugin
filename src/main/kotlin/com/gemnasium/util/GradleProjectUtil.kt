package com.gemnasium.util

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration

/**
 * Checks whether the given project should be scanned
 * because either scanProjects is empty or it contains the
 * project's path.
 */
fun shouldBeScanned(project: Project): Boolean {
    //!config.scanProjects || config.scanProjects.contains(project.path)
    return true
}

/**
 * Checks whether the given project should be skipped
 * because skipProjects contains the project's path.
 */
fun shouldBeSkipped(project: Project): Boolean {
    //config.skipProjects.contains(project.path)
    return false
}

/**
 * Checks whether the given configuration should be scanned
 * because either scanConfigurations is empty or it contains the
 * configuration's name.
 */
fun shouldBeScanned(configuration: Configuration): Boolean {
    //!config.scanConfigurations || config.scanConfigurations.contains(configuration.name)
    return true
}

/**
 * Checks whether the given configuration should be skipped
 * because skipConfigurations contains the configuration's name.
 */
fun shouldBeSkipped(configuration: Configuration): Boolean {
    //config.skipConfigurations.contains(configuration.name)
    return false
}

/**
 * Checks whether the given configuration should be skipped
 * because it is a test configuration and skipTestGroups is true.
 */
fun shouldBeSkippedAsTest(configuration: Configuration): Boolean {
    //config.skipTestGroups && isTestConfiguration(configuration)
    return false
}

/**
 * Determines if the configuration should be considered a test configuration.
 * @param configuration the configuration to insepct
 * @return true if the configuration is considered a tet configuration; otherwise false
 */
fun isTestConfiguration(configuration: Configuration): Boolean {
//def hierarchy = configuration.hierarchy.collect({ it.name }).join(" --> ")
    //logger.info("'{}' is considered a test configuration: {}", hierarchy, isTestConfiguration)
    return isTestConfigurationCheck(configuration)
}

/**
 * Checks whether a configuration is considered to be a test configuration in order to skip it.
 * A configuration is considered a test configuration if and only if any of the following conditions holds:
 *
 *  * the name of the configuration or any of its parent configurations equals 'testCompile'
 *  * the name of the configuration or any of its parent configurations equals 'androidTestCompile'
 *  * the configuration name starts with 'test'
 *  * the configuration name starts with 'androidTest'
 *
 */
fun isTestConfigurationCheck(configuration: Configuration): Boolean {
    var isTestConfiguration = configuration.name.startsWith("test") || configuration.name.startsWith("androidTest")
    for (conf in configuration.hierarchy) {
        isTestConfiguration = isTestConfiguration or (conf.name === "testCompile" || conf.name === "androidTestCompile")
    }
    return isTestConfiguration
}

/**
 * Determines if the onfiguration can be resolved
 * @param configuration the configuration to inspect
 * @return true if the configuration can be resolved; otherwise false
 */
fun canBeResolved(configuration: Configuration): Boolean {
    return configuration.isCanBeResolved
}