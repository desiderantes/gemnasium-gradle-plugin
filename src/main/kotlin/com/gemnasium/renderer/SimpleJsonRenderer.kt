package com.gemnasium.renderer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.gemnasium.model.DependencyNode
import java.io.File

class SimpleJsonRenderer : Renderer {

    private val mapper = ObjectMapper()
    val dependencyNode = mapper.createArrayNode()

    override fun addDependencies(dependenciesList: Collection<DependencyNode>) {

        dependenciesList.forEach {
            dependencyNode.add(createDependencyNode(mapper, it))
        }
    }

    override fun writeToFile(file:File) {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, dependencyNode)
    }

    private fun createDependencyNode(mapper: ObjectMapper, dependencyNode: DependencyNode): ObjectNode {
        val node = mapper.createObjectNode()
        node.put("groupId", dependencyNode.groupId)
        node.put("artifactId", dependencyNode.artifactId)
        node.put("version", dependencyNode.version)

        node.put("scope", dependencyNode.configuration)
        node.put("transitive", !dependencyNode.isDirectDependency)
        if (dependencyNode.requested != null) {
            node.put("requirement", dependencyNode.requested)
        }
        node.set<ObjectNode>("parents", mapper.valueToTree<ObjectNode>(dependencyNode.parents))
        return node
    }


}