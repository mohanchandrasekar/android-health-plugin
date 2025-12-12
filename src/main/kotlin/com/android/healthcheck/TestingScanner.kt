package com.android.healthcheck

import java.io.File

class TestingScanner {

    fun scanProject(rootDir: File): List<Finding> {
        var mainSourceCount = 0
        var unitTestCount = 0
        var androidTestCount = 0

        rootDir.walkTopDown()
            .filter {
                it.isFile && (it.extension.equals("kt", true) || it.extension.equals(
                    "java",
                    true
                ))
            }
            .forEach { file ->
                val path = file.path.replace("\\", "/")
                when {
                    path.contains("/src/test/", ignoreCase = true) -> unitTestCount++
                    path.contains("/src/androidTest/", ignoreCase = true) -> androidTestCount++
                    path.contains("/src/main/", ignoreCase = true) -> mainSourceCount++
                }
            }

        val findings = mutableListOf<Finding>()

        if (unitTestCount == 0 && androidTestCount == 0) {
            findings += Finding(
                ruleId = "Q4",
                category = "Quality",
                severity = "Warning",
                file = rootDir.path,
                message = "No tests detected (neither unit tests nor instrumentation tests). " +
                        "Add unit tests for ViewModels/use-cases and instrumentation tests only where Android components are needed."
            )
            return findings
        }

        if (unitTestCount == 0 && androidTestCount > 0) {
            findings += Finding(
                ruleId = "Q4",
                category = "Quality",
                severity = "Info",
                file = rootDir.path,
                message = "Only instrumentation (androidTest) tests detected, no unit tests in src/test. " +
                        "Consider adding unit tests for business logic (ViewModels, domain, repositories)."
            )
        }

        if (mainSourceCount > 0) {
            val totalTests = unitTestCount + androidTestCount
            val ratio = totalTests.toDouble() / mainSourceCount.toDouble()
            if (ratio < 0.1) {
                findings += Finding(
                    ruleId = "Q4",
                    category = "Quality",
                    severity = "Info",
                    file = rootDir.path,
                    message = "Low test-to-code ratio: $totalTests tests for $mainSourceCount main source files. " +
                            "Consider strengthening your testing strategy to avoid regressions."
                )
            }
        }
        return findings
    }
}
