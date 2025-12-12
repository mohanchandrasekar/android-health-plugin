package com.android.healthcheck

import org.yaml.snakeyaml.Yaml
import java.io.File

object HealthConfigLoader {

    @Suppress("UNCHECKED_CAST")
    fun load(rootDir: File): HealthConfig {
        val file = File(rootDir, "android-health.yml")
        if (!file.exists()) return HealthConfig()

        val yaml = Yaml()
        val map = yaml.load<Map<String, Any?>>(file.readText()) ?: emptyMap()

        fun bool(section: String, key: String, default: Boolean): Boolean {
            val s = map[section] as? Map<*, *> ?: return default
            return (s[key] as? Boolean) ?: default
        }

        fun int(section: String, key: String, default: Int): Int {
            val s = map[section] as? Map<*, *> ?: return default
            return when (val v = s[key]) {
                is Number -> v.toInt()
                is String -> v.toIntOrNull() ?: default
                else -> default
            }
        }

        fun listOfStrings(m: Map<*, *>, key: String): List<String> {
            val v = m[key] ?: return emptyList()
            return when (v) {
                is List<*> -> v.filterIsInstance<String>()
                is String -> listOf(v)
                else -> emptyList()
            }
        }

        val archMap = map["architecture"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val cleanMap = archMap["cleanLayers"] as? Map<*, *> ?: emptyMap<Any, Any>()

        val secMap = map["security"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val logMap = secMap["logging"] as? Map<*, *> ?: emptyMap<Any, Any>()

        val qualMap = map["quality"] as? Map<*, *> ?: emptyMap<Any, Any>()
        val rulesMap = map["rules"] as? Map<*, *> ?: emptyMap<Any, Any>()

        val patternStr = archMap["defaultPattern"] as? String ?: "MVVM"
        val pattern = runCatching {
            ArchitecturePattern.valueOf(patternStr.uppercase())
        }.getOrDefault(ArchitecturePattern.MVVM)

        return HealthConfig(
            architecture = HealthConfig.Architecture(
                maxActivityLines = int("architecture", "maxActivityLines", 400),
                enabled = bool("architecture", "enabled", true),
                defaultPattern = pattern,
                cleanLayers = HealthConfig.CleanLayers(
                    uiPackages = listOfStrings(cleanMap, "uiPackages"),
                    domainPackages = listOfStrings(cleanMap, "domainPackages"),
                    dataPackages = listOfStrings(cleanMap, "dataPackages")
                )
            ),
            security = HealthConfig.Security(
                failOnHardcodedSecrets = bool("security", "failOnHardcodedSecrets", true),
                checkSharedPrefs = bool("security", "checkSharedPrefs", true),
                logging = HealthConfig.Logging(
                    allowDirectAndroidLog = (logMap["allowDirectAndroidLog"] as? Boolean) ?: false,
                    allowedLogWrappers = listOfStrings(logMap, "allowedLogWrappers"),
                    sensitiveKeywords = listOfStrings(logMap, "sensitiveKeywords")
                        .ifEmpty { HealthConfig.Logging().sensitiveKeywords }
                )
            ),
            quality = HealthConfig.Quality(
                maxFunctionLines = (qualMap["maxFunctionLines"] as? Number)?.toInt() ?: 40,
                maxComplexity = (qualMap["maxComplexity"] as? Number)?.toInt() ?: 10,
                enabled = (qualMap["enabled"] as? Boolean) ?: true
            ),
            rules = HealthConfig.Rules(
                A1_GodActivity = (rulesMap["A1_GodActivity"] as? Boolean) ?: true,
                S1_HardcodedSecrets = (rulesMap["S1_HardcodedSecrets"] as? Boolean) ?: true,
                S2_InsecurePrefs = (rulesMap["S2_InsecurePrefs"] as? Boolean) ?: true,
                S3_ExportedComponent = (rulesMap["S3_ExportedComponent"] as? Boolean) ?: true
            )
        )
    }
}
