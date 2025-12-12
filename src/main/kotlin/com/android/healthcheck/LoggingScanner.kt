package com.android.healthcheck

import java.io.File

class LoggingScanner(
    private val config: HealthConfig
) {
    private val logConfig = config.security.logging

    fun scan(file: File): List<Finding> {
        if (!file.extension.equals("kt", ignoreCase = true) &&
            !file.extension.equals("java", ignoreCase = true)
        ) return emptyList()

        val findings = mutableListOf<Finding>()
        val lines = file.readLines()
        val imports = KotlinFileUtils.imports(file)
        val usesAndroidLogImport = imports.any { it == "android.util.Log" }

        if (usesAndroidLogImport && !logConfig.allowDirectAndroidLog) {
            findings += Finding(
                ruleId = "S4",
                category = "Security",
                severity = "Warning",
                file = file.path,
                message = "File imports android.util.Log. Avoid direct Log usage; " +
                          "use a central logger so logs can be controlled/redacted in release builds."
            )
        }

        lines.forEachIndexed { index, line ->
            val trimmed = line.trim()

            if (isLogCall(trimmed)) {
                if (!logConfig.allowDirectAndroidLog && trimmed.startsWith("Log.")) {
                    findings += Finding(
                        ruleId = "S4",
                        category = "Security",
                        severity = "Warning",
                        file = "${file.path}:${index + 1}",
                        message = "Direct android.util.Log usage. Use project logger abstraction instead."
                    )
                }

                val lower = trimmed.lowercase()
                val sensitiveHit = logConfig.sensitiveKeywords.firstOrNull {
                    lower.contains(it.lowercase())
                }
                if (sensitiveHit != null) {
                    findings += Finding(
                        ruleId = "S5",
                        category = "Security",
                        severity = "Error",
                        file = "${file.path}:${index + 1}",
                        message = "Log statement appears to include sensitive data keyword \"$sensitiveHit\". " +
                                "Do NOT log tokens, passwords, PII, or secrets in debug or production."
                    )
                }
            }
        }
        return findings
    }

    private fun isLogCall(line: String): Boolean {
        if (line.isEmpty()) return false
        if (line.startsWith("Log.") || line.contains(" Log.") ||
            line.startsWith("Timber.") || line.contains(" Timber.")
        ) return true

        if (logConfig.allowedLogWrappers.isNotEmpty()) {
            val classNames = logConfig.allowedLogWrappers.map { it.substringAfterLast('.') }
            if (classNames.any { cls -> line.startsWith("$cls.") || line.contains(" $cls.") }) {
                return true
            }
        }
        return false
    }
}
