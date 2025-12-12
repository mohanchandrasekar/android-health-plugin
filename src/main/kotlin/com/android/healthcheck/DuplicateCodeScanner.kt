package com.android.healthcheck

import java.io.File

class DuplicateCodeScanner {

    private data class Location(val filePath: String, val startLine: Int)

    fun scanProject(rootDir: File): List<Finding> {
        val windowMap = mutableMapOf<String, MutableList<Location>>()

        rootDir.walkTopDown()
            .filter { it.isFile && it.extension.equals("kt", ignoreCase = true) }
            .forEach { file ->
                collectWindows(file, windowMap)
            }

        val findings = mutableListOf<Finding>()

        windowMap.values
            .filter { it.size > 1 }
            .forEach { locations ->
                locations.forEach { loc ->
                    findings += Finding(
                        ruleId = "Q3",
                        category = "Quality",
                        severity = "Warning",
                        file = "${loc.filePath}:${loc.startLine}",
                        message = "Possible duplicate logic detected. Similar 5+ line block appears in ${locations.size} locations. " +
                                "Consider extracting shared helpers/mappers or base use-cases instead of copy-paste."
                    )
                }
            }
        return findings
    }

    private fun collectWindows(file: File, windowMap: MutableMap<String, MutableList<Location>>) {
        val rawLines = file.readLines()

        data class CodeLine(val normalized: String, val originalLine: Int)

        val codeLines = mutableListOf<CodeLine>()
        rawLines.forEachIndexed { index, raw ->
            val trimmed = raw.trim()
            if (trimmed.isEmpty()) return@forEachIndexed
            if (trimmed.startsWith("//")) return@forEachIndexed
            codeLines += CodeLine(trimmed, index + 1)
        }

        val windowSize = 5
        if (codeLines.size < windowSize) return

        for (i in 0..(codeLines.size - windowSize)) {
            val window = codeLines.subList(i, i + windowSize)
            val key = window.joinToString("\n") { it.normalized }
            val loc = Location(file.path, window.first().originalLine)
            val list = windowMap.getOrPut(key) { mutableListOf() }
            list.add(loc)
        }
    }
}
