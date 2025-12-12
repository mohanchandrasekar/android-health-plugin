package com.android.healthcheck

import java.io.File

class StaticAnalysisScanner {
    fun scanProject(rootDir: File): List<Finding> {
        var foundDetekt = false
        var foundKtlint = false

        rootDir.walkTopDown()
            .filter { it.isFile }
            .forEach { file ->
                val name = file.name
                if (name == "detekt.yml" || name == "detekt-config.yml") foundDetekt = true
                if (name.contains("ktlint", ignoreCase = true)) foundKtlint = true

                if (name.endsWith(".gradle") || name.endsWith(".gradle.kts")) {
                    val text = file.readText()
                    if (!foundDetekt && text.contains("detekt", ignoreCase = true)) foundDetekt = true
                    if (!foundKtlint && text.contains("ktlint", ignoreCase = true)) foundKtlint = true
                }
            }

        if (foundDetekt || foundKtlint) return emptyList()

        return listOf(
            Finding(
                ruleId = "Q0",
                category = "Quality",
                severity = "Info",
                file = rootDir.path,
                message = "No Detekt/Ktlint configuration detected. Consider adding static analysis tools " +
                          "for style and code smells (Detekt, Ktlint, Android Lint in CI)."
            )
        )
    }
}
