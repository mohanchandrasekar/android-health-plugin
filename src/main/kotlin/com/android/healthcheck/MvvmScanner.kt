package com.android.healthcheck

import java.io.File

class MvvmScanner(
    private val config: HealthConfig
) {
    private val clean = config.architecture.cleanLayers

    fun scan(file: File): List<Finding> {
        if (!file.extension.equals("kt", ignoreCase = true) &&
            !file.extension.equals("java", ignoreCase = true)
        ) return emptyList()

        val fileName = file.nameWithoutExtension
        val findings = mutableListOf<Finding>()

        if (fileName.endsWith("Activity") || fileName.endsWith("Fragment")) {
            findings += checkViewImportsData(file)
        }

        if (fileName.endsWith("ViewModel")) {
            findings += checkViewModelAndroidDependencies(file)
        }
        return findings
    }

    private fun checkViewImportsData(file: File): List<Finding> {
        val imports = KotlinFileUtils.imports(file)
        val dataPrefixes = clean.dataPackages
        val hits = mutableListOf<String>()

        imports.forEach { imp ->
            if (dataPrefixes.any { prefix -> imp.startsWith(prefix) }) hits += imp
            if (imp.startsWith("retrofit2.") ||
                imp.startsWith("okhttp3.") ||
                imp.contains("room", ignoreCase = true)
            ) hits += imp
        }

        if (hits.isEmpty()) return emptyList()

        return listOf(
            Finding(
                ruleId = "MV1",
                category = "Architecture",
                severity = "Warning",
                file = file.path,
                message = "View (Activity/Fragment) imports data-layer or networking types: ${hits.distinct().joinToString()}. " +
                          "In MVVM, Views should delegate to ViewModels/UseCases instead of talking to data layer directly."
            )
        )
    }

    private fun checkViewModelAndroidDependencies(file: File): List<Finding> {
        val imports = KotlinFileUtils.imports(file)
        val lines = file.readLines()

        val badImports = imports.filter { imp ->
            imp.startsWith("android.") ||
            imp.startsWith("androidx.fragment.") ||
            imp.startsWith("android.view.") ||
            imp.startsWith("android.widget.")
        }

        val constructorLine = lines.firstOrNull { line ->
            line.contains("class ") && line.contains("ViewModel") && line.contains("(")
        }

        val ctorBadTypes = mutableListOf<String>()
        constructorLine?.let { line ->
            val paramsPart = line.substringAfter("(", "").substringBeforeLast(")", "")
            val tokens = paramsPart.split(",", ":", "=", " ")
                .map { it.trim() }
                .filter { it.isNotEmpty() }

            val banned = listOf("Context", "Activity", "Fragment", "View")
            tokens.forEach { token ->
                if (token in banned || banned.any { b -> token.endsWith(".$b") }) {
                    ctorBadTypes += token
                }
            }
        }

        if (badImports.isEmpty() && ctorBadTypes.isEmpty()) return emptyList()

        val problems = mutableListOf<String>()
        if (badImports.isNotEmpty()) problems += "imports Android UI/Framework types: ${badImports.joinToString()}"
        if (ctorBadTypes.isNotEmpty()) problems += "constructor depends on framework types: ${ctorBadTypes.joinToString()}"

        return listOf(
            Finding(
                ruleId = "MV2",
                category = "Architecture",
                severity = "Warning",
                file = file.path,
                message = "ViewModel should not depend on Android framework directly, but found ${problems.joinToString(" and ")}. " +
                          "Inject interfaces or plain value objects instead to keep ViewModels testable."
            )
        )
    }
}
