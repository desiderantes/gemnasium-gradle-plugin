package com.gemnasium.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.gemnasium.util.*
import org.gradle.api.DefaultTask
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.component.ModuleComponentSelector
import org.gradle.api.artifacts.result.DependencyResult
import org.gradle.api.artifacts.result.ResolvedDependencyResult
import org.gradle.api.artifacts.result.UnresolvedDependencyResult
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.IOException
import java.util.ArrayList
import java.util.HashSet
import java.util.LinkedHashMap

open class DumpDependenciesTask : DefaultTask() {

    companion object {
        @Internal
        const val TASK_NAME = "gemnasiumDumpDependencies"
    }

    @TaskAction
    internal fun walk() {
        val directDependencies = HashSet<String>()
        val processedDependencies = HashSet<String>()
        val configurationDependencies = LinkedHashMap<Configuration, Set<DependencyResult>>()

        project.configurations.stream().filter { conf -> shouldBeScanned(conf) && !(shouldBeSkipped(conf) || shouldBeSkippedAsTest(conf)) && canBeResolved(conf) }.forEach { configuration ->
            val resolutionResult = configuration.incoming.resolutionResult
            val root = resolutionResult.root

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

        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(System.out, dependenciesJsonNode)
            mapper.writerWithDefaultPrettyPrinter().writeValue(File("foo.json"), dependenciesJsonNode)
        } catch (e: IOException) {
            e.printStackTrace()
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
        node.set("parents", mapper.valueToTree(parents))
        return node
    }

    private fun isDirectDependency(parents: List<String>): Boolean {
        return parents.isEmpty()
    }
}