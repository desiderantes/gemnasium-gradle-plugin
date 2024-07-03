package com.gemnasium.model

data class DependencyNode(
    val configuration: String,
    val groupId: String,
    val artifactId: String,
    val version: String,
    val isDirectDependency: Boolean,
    val requested: String?,
    val parents: List<String>
)
