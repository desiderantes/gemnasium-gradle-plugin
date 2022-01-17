package com.gemnasium.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.gemnasium.extension.GemnasiumGradlePluginExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.tasks.*
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashMap

open class DumpDependenciesTask : DefaultTask() {

    companion object {
        const val TASK_NAME = "gemnasiumDumpDependencies"
    }

    // The @OutputFile annotation is disabled until I find a way to specify an input dependency on the Gradle project
    // itself. Currently, the output file will not get re-generated if the dependencies of the project change so it's
    // better to not specify any inputs/outputs to force gradle to always consider this task out-of-date.
    private val outputFile: File
    //@OutputFile
    get() {
        val ext = project.extensions.getByType(GemnasiumGradlePluginExtension::class.java)
        return File(ext.outputDir, ext.outputFileName)
    }


//    /**
//     * Checks whether the given project should be scanned
//     * because either scanProjects is empty or it contains the
//     * project's path.
//     */
//    fun shouldBeScanned(project: Project): Boolean {
//        //!config.scanProjects || config.scanProjects.contains(project.path)
//        return true
//    }
//
//    /**
//     * Checks whether the given project should be skipped
//     * because skipProjects contains the project's path.
//     */
//    fun shouldBeSkipped(project: Project): Boolean {
//        //config.skipProjects.contains(project.path)
//        return false
//    }

    /**
     * Checks whether the given configuration should be scanned
     * because either scanConfigurations is empty or it contains the
     * configuration's name.
     */
    private fun shouldBeScanned(configuration: Configuration): Boolean {
        val ext = project.extensions.getByType(GemnasiumGradlePluginExtension::class.java)
        return ext.scanConfigurations.isEmpty() || ext.scanConfigurations.contains(configuration.name)
    }

    /**
     * Checks whether the given configuration should be skipped
     * because skipConfigurations contains the configuration's name.
     */
    private fun shouldBeSkipped(configuration: Configuration): Boolean {
        val ext = project.extensions.getByType(GemnasiumGradlePluginExtension::class.java)
        return ext.skipConfigurations.contains(configuration.name)
    }

    /**
     * Checks whether the given configuration should be skipped
     * because it is a test configuration and skipTestGroups is true.
     */
    private fun shouldBeSkippedAsTest(configuration: Configuration): Boolean {
        val ext = project.extensions.getByType(GemnasiumGradlePluginExtension::class.java)
        return ext.skipTestGroups && isTestConfiguration(configuration)
    }

    /**
     * Determines if the configuration should be considered a test configuration.
     * @param configuration the configuration to insepct
     * @return true if the configuration is considered a tet configuration; otherwise false
     */
    private fun isTestConfiguration(configuration: Configuration): Boolean {
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
    private fun isTestConfigurationCheck(configuration: Configuration): Boolean {
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
    private fun canBeResolved(configuration: Configuration): Boolean {
        return configuration.isCanBeResolved
    }

    @TaskAction
    internal fun walk() {
        if (project.extensions.findByType(GemnasiumGradlePluginExtension::class.java)!!.skip) {
            logger.lifecycle("Skipping ${TASK_NAME}")
            return
        }

        val directDependencies = HashSet<String>()
        val processedDependencies = HashSet<String>()
        val configurationDependencies = LinkedHashMap<Configuration, Set<DependencyResult>>()

        project.configurations.filter {
            shouldBeScanned(it) && !(shouldBeSkipped(it) || shouldBeSkippedAsTest(it)) && canBeResolved(it)
        }.forEach { configuration ->
            val resolutionResult = configuration.incoming.resolutionResult
            val root = resolutionResult.root
            val unresolvedDeps = root.dependencies.filterIsInstance<UnresolvedDependencyResult>()

            if (unresolvedDeps.isNotEmpty()) {
                val unresolvedDepNames = unresolvedDeps.map { it.requested.displayName }
                val unresolvedDepNamesFormatted = unresolvedDepNames.joinToString(separator = ", ")

                throw GradleException("Project has ${unresolvedDeps.size} unresolved dependencies: ${unresolvedDepNamesFormatted}")
            }

            // Keep track of all direct dependencies
            directDependencies.addAll(root.dependencies.map { it.requested.displayName })

            // Add all direct dependencies of this configuration to a map to be recursively processed later
            configurationDependencies[configuration] = root.dependencies
        }

        val mapper = ObjectMapper()
        val dependenciesJsonNode = mapper.createArrayNode()

        // Finally process all configuration dependencies recursively
        configurationDependencies.forEach { (configuration, dependencies) ->
            traverseDependencies(mapper, dependenciesJsonNode, ArrayList(),
                    directDependencies, processedDependencies, configuration, dependencies)
        }

        try {
            if (dependenciesJsonNode.size() > 0) {
                logger.quiet("Writing dependency JSON to ${outputFile}")
                outputFile.parentFile.mkdirs()
                //outputFile.createNewFile()
                mapper.writerWithDefaultPrettyPrinter().writeValue(outputFile, dependenciesJsonNode)
            } else {
                logger.quiet("No dependencies found in project")
            }
        } catch (e: IOException) {
            throw GradleException("Could not write output file", e)
        }
    }

    private fun traverseDependencies(mapper: ObjectMapper, dependenciesJsonNode: ArrayNode,
                                     parents: List<String>, directDependencies: Set<String>,
                                     processedDependencies: MutableSet<String>, configuration: Configuration,
                                     dependencies: Set<DependencyResult>) {
        for (dependency in dependencies) {
            if (dependency is ResolvedDependencyResult) {
                val componentResult = dependency.selected
                val componentIdentifier = componentResult.id
                val componentName = componentIdentifier.displayName
                val isDirectDependency = isDirectDependency(parents)

                // If we have already processed this dependency, we skip it.
                if (processedDependencies.contains(componentName)) {
                    continue
                }
                // If the dependency being processed is transitive but it's in the directDependencies set, we skip it
                // because we want to include the direct dependency rather than the transitive one.
                if (!isDirectDependency && directDependencies.contains(componentName)) {
                    continue
                }

                // Add this dependency to our list and mark it as processed
                processedDependencies.add(componentName)

                val moduleVersion = componentResult.moduleVersion
                if (moduleVersion != null) {
                    var requested: String? = null
                    if (dependency.getRequested() is ModuleComponentSelector) {
                        requested = (dependency.getRequested() as ModuleComponentSelector).version
                    }
                    val node = createDependencyNode(mapper, configuration.name, moduleVersion.group,
                            moduleVersion.name, moduleVersion.version, isDirectDependency, requested, parents)
                    dependenciesJsonNode.add(node)
                }

                val nextGeneration = ArrayList(parents)

                nextGeneration.add(componentName)

                traverseDependencies(mapper, dependenciesJsonNode, nextGeneration, directDependencies,
                        processedDependencies, configuration, componentResult.dependencies)
            } else if (dependency is UnresolvedDependencyResult) {
                val componentSelector = dependency.attempted
                logger.debug("Ignoring unresolved dependency: " + componentSelector.displayName)
            }
        }
    }

    private fun createDependencyNode(mapper: ObjectMapper, configuration: String, groupId: String, artifactId: String,
                                     version: String, isDirectDependency: Boolean, requested: String?,
                                     parents: List<String>): ObjectNode {
        val node = mapper.createObjectNode()
        node.put("groupId", groupId)
        node.put("artifactId", artifactId)
        node.put("version", version)

        node.put("scope", configuration)
        node.put("transitive", !isDirectDependency)
        if (requested != null) {
            node.put("requirement", requested)
        }
        node.set<ObjectNode>("parents", mapper.valueToTree<ObjectNode>(parents))
        return node
    }

    private fun isDirectDependency(parents: List<String>): Boolean {
        return parents.isEmpty()
    }
}
