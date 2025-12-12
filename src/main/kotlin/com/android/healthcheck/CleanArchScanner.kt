package com.android.healthcheck

import java.io.File

class CleanArchScanner(
    private val config: HealthConfig
) {
    private val layers = config.architecture.cleanLayers

    fun scan(file: File): List<Finding> {
        if (!file.extension.equals("kt", ignoreCase = true) &&
            !file.extension.equals("java", ignoreCase = true)
        ) return emptyList()

        val pkg = KotlinFileUtils.packageName(file) ?: return emptyList()
        val imports = KotlinFileUtils.imports(file)
        val findings = mutableListOf<Finding>()

        if (isInAny(pkg, layers.domainPackages)) {
            findings += checkDomainImports(file, imports)
        }
        if (isInAny(pkg, layers.uiPackages)) {
            findings += checkUiImportsData(file, imports)
        }
        return findings
    }

    private fun isInAny(pkg: String, prefixes: List<String>): Boolean =
        prefixes.any { prefix -> pkg.startsWith(prefix) }

    private fun checkDomainImports(file: File, imports: List<String>): List<Finding> {
        val bad = mutableListOf<String>()
        imports.forEach { imp ->
            if (imp.startsWith("android.") || imp.startsWith("androidx.")) bad += imp
            if (config.architecture.cleanLayers.uiPackages.any { ui -> imp.startsWith(ui) }) bad += imp
        }
        if (bad.isEmpty()) return emptyList()

        return listOf(
            Finding(
                ruleId = "CL1",
                category = "Architecture",
                severity = "Error",
                file = file.path,
                message = "Domain layer should not depend on Android or UI, but imports: ${bad.distinct().joinToString()}. " +
                          "Keep domain pure and free of framework dependencies."
            )
        )
    }

    private fun checkUiImportsData(file: File, imports: List<String>): List<Finding> {
        val bad = imports.filter { imp ->
            layers.dataPackages.any { dataPrefix -> imp.startsWith(dataPrefix) }
        }
        if (bad.isEmpty()) return emptyList()

        return listOf(
            Finding(
                ruleId = "CL3",
                category = "Architecture",
                severity = "Warning",
                file = file.path,
                message = "UI layer imports Data layer directly: ${bad.joinToString()}. " +
                          "In Clean Architecture, UI should depend on domain/use-case interfaces, not concrete data implementations."
            )
        )
    }
}
