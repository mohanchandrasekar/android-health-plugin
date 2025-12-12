package com.android.healthcheck

import java.io.File

class SecretScanner {

    private val suspiciousKeywords = listOf(
        "token", "secret", "api_key", "authorization", "bearer "
    )

    fun scan(file: File): List<Finding> {
        val findings = mutableListOf<Finding>()

        file.readLines().forEachIndexed { index, line ->
            val lower = line.lowercase()

            if (suspiciousKeywords.any { lower.contains(it) } && line.contains("\"")) {
                val quoted = Regex("\"([^\"]+)\"")
                    .findAll(line)
                    .map { it.groupValues[1] }

                quoted.forEach { content ->
                    if (content.length > 20 && hasLettersAndDigits(content)) {
                        findings += Finding(
                            ruleId = "S1",
                            category = "Security",
                            severity = "Error",
                            file = "${file.path}:${index + 1}",
                            message = "Possible hardcoded secret -> \"$content\". " +
                                    "Remove secrets from source; keep them on the backend or in secure config."
                        )
                    }
                }
            }
        }
        return findings
    }

    private fun hasLettersAndDigits(s: String): Boolean {
        val hasLetter = s.any { it.isLetter() }
        val hasDigit = s.any { it.isDigit() }
        return hasLetter && hasDigit
    }
}
