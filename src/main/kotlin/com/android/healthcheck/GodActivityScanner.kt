package com.android.healthcheck

import java.io.File

class GodActivityScanner(
    private val maxLines: Int
) {
    fun scan(file: File): List<Finding> {
        val name = file.nameWithoutExtension
        val isActivityOrFragment = name.endsWith("Activity") || name.endsWith("Fragment")
        if (!isActivityOrFragment) return emptyList()

        val lineCount = file.readLines().size
        if (lineCount <= maxLines) return emptyList()

        return listOf(
            Finding(
                ruleId = "A1",
                category = "Architecture",
                severity = "Warning",
                file = file.path,
                message = "God Activity/Fragment: ${file.path} has $lineCount lines (> $maxLines). " +
                          "Move business logic into ViewModels/use-cases and keep UI classes lean."
            )
        )
    }
}
