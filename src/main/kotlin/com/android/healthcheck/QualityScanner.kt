package com.android.healthcheck

import java.io.File

class QualityScanner(
    private val config: HealthConfig
) {
    private val maxLines = config.quality.maxFunctionLines
    private val maxComplexity = config.quality.maxComplexity

    fun scan(file: File): List<Finding> {
        if (!file.extension.equals("kt", ignoreCase = true)) return emptyList()

        val findings = mutableListOf<Finding>()
        val lines = file.readLines()

        var i = 0
        while (i < lines.size) {
            val line = lines[i]
            if (line.trimStart().startsWith("fun ")) {
                val startIndex = i
                var endIndex = lines.size - 1
                var j = i + 1
                while (j < lines.size) {
                    val trimmed = lines[j].trimStart()
                    if (trimmed.startsWith("fun ")) {
                        endIndex = j - 1
                        break
                    }
                    j++
                }

                val functionLines = endIndex - startIndex + 1
                val functionBody = lines.subList(startIndex, endIndex + 1)
                val header = lines[startIndex].trim()

                if (functionLines > maxLines) {
                    findings += Finding(
                        ruleId = "Q1",
                        category = "Quality",
                        severity = "Warning",
                        file = "${file.path}:${startIndex + 1}",
                        message = "Function appears long ($functionLines lines > $maxLines). " +
                                "Consider splitting into smaller functions with single responsibilities. " +
                                "Header: \"$header\""
                    )
                }

                val complexity = estimateComplexity(functionBody)
                if (complexity > maxComplexity) {
                    findings += Finding(
                        ruleId = "Q2",
                        category = "Quality",
                        severity = "Warning",
                        file = "${file.path}:${startIndex + 1}",
                        message = "Function has high estimated complexity ($complexity > $maxComplexity). " +
                                "Try reducing nested conditionals/branches or extracting helpers. " +
                                "Header: \"$header\""
                    )
                }
                i = endIndex + 1
            } else {
                i++
            }
        }
        return findings
    }

    private fun estimateComplexity(lines: List<String>): Int {
        var score = 0
        val patterns = listOf(
            Regex("\\bif\\b"),
            Regex("\\belse if\\b"),
            Regex("\\bwhen\\b"),
            Regex("\\bfor\\b"),
            Regex("\\bwhile\\b"),
            Regex("&&"),
            Regex("\\|\\|")
        )

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("//")) return@forEach
            if (trimmed.startsWith("/*")) return@forEach
            patterns.forEach { regex ->
                score += regex.findAll(trimmed).count()
            }
        }
        return 1 + score
    }
}
