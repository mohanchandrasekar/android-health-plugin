package com.android.healthcheck

import java.io.File

object KotlinFileUtils {

    fun packageName(file: File): String? =
        file.useLines { lines ->
            lines.firstOrNull { it.trim().startsWith("package ") }
                ?.removePrefix("package")
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        }

    fun imports(file: File): List<String> {
        val result = mutableListOf<String>()
        file.forEachLine { line ->
            val trimmed = line.trim()
            if (trimmed.startsWith("import ")) {
                val imp = trimmed.removePrefix("import").trim()
                if (imp.isNotEmpty()) result += imp
            }
        }
        return result
    }
}
