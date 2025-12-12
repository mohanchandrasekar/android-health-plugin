package com.android.healthcheck

import java.io.File

class ManifestScanner {
    fun scan(file: File): List<Finding> {
        val findings = mutableListOf<Finding>()
        val text = file.readText()

        val regex = Regex("<(activity|service|receiver|provider)[^>]*exported=\"true\"[^>]*>")
        regex.findAll(text).forEach { match ->
            val tag = match.value
            val hasPermission = tag.contains("android:permission=")

            if (!hasPermission) {
                findings += Finding(
                    ruleId = "S3",
                    category = "Security",
                    severity = "Warning",
                    file = file.path,
                    message = "Exported component without explicit permission: $tag. " +
                              "Ensure exported components are intentional and protected."
                )
            }
        }
        return findings
    }
}
