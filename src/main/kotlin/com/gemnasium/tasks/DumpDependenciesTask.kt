package com.gemnasium.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction

open class DumpDependenciesTask : DefaultTask() {

    companion object {
        @Internal
        const val TASK_NAME = "gemnasiumDumpDependencies"
    }

    @TaskAction
    fun dumpDependencies() {
        println("hello from dump dependencies in kotlin")
    }
}