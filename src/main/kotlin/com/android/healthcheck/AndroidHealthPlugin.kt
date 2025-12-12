package com.android.healthcheck

import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidHealthPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        if (project != project.rootProject) return

        val t = project.tasks.register("androidHealthCheck", AndroidHealthTask::class.java)
        t.configure {
            group = "verification"
            description =
                "Runs architecture, code quality and security checks for Android projects."
        }
    }
}
