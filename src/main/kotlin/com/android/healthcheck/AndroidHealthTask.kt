package com.android.healthcheck

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class AndroidHealthTask : DefaultTask() {

    @TaskAction
    fun run() {
        val rootDir: File = project.rootDir
        val config = HealthConfigLoader.load(rootDir)

        val reportDir = File(rootDir, "build/reports/android-health")
        reportDir.mkdirs()

        val txtReportFile = File(reportDir, "report.txt")
        val htmlReportFile = File(reportDir, "report.html")

        val analyzer = AndroidHealthAnalyzer(config)
        val findings = analyzer.analyze(rootDir)

        txtReportFile.printWriter().use { out ->
            out.println("=== Android Health Report ===")
            out.println("Project: ${project.name}")
            out.println("Generated at: ${java.time.Instant.now()}")
            out.println()

            if (findings.isEmpty()) {
                out.println("✅ No issues found by v0.1 rules.")
            } else {
                findings.forEach { f ->
                    out.println("- [${f.severity}] ${f.ruleId} @ ${f.file} — ${f.message}")
                }
            }
        }

        HtmlReporter().write(htmlReportFile, findings)

        println("Android Health report written to:")
        println(" - ${txtReportFile.absolutePath}")
        println(" - ${htmlReportFile.absolutePath}")
    }
}
