package com.gemnasium.renderer

import com.gemnasium.model.DependencyNode
import java.io.File

interface Renderer {
    fun addDependencies(dependenciesList: Collection<DependencyNode>)
    fun writeToFile(file: File)
}