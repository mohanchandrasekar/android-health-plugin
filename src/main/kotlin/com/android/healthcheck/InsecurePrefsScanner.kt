package com.android.healthcheck

import java.io.File

class InsecurePrefsScanner {
    private val sensitiveKeys = listOf("token", "password", "session", "user", "auth")

    fun scan(file: File): List<Finding> {
        val findings = mutableListOf<Finding>()
        val lines = file.readLines()

        lines.forEachIndexed { index, line ->
            val lower = line.lowercase()
            val usesPrefs =
                lower.contains("getsharedpreferences(") ||
                lower.contains("defaultsharedpreferences(")

            if (!usesPrefs) return@forEachIndexed

            val window = (index - 2).coerceAtLeast(0)..(index + 2).coerceAtMost(lines.lastIndex)
            val snippet = window.map { lines[it].lowercase() }.joinToString(" ")

            if (sensitiveKeys.any { snippet.contains(it) }) {
                findings += Finding(
                    ruleId = "S2",
                    category = "Security",
                    severity = "Warning",
                    file = "${file.path}:${index + 1}",
                    message = "Possible sensitive data stored in SharedPreferences near this location. " +
                              "Use EncryptedSharedPreferences or avoid storing long-lived secrets on device."
                )
            }
        }
        return findings
    }
}
