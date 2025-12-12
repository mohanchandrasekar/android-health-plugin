package com.android.healthcheck

data class HealthConfig(
    val architecture: Architecture = Architecture(),
    val security: Security = Security(),
    val quality: Quality = Quality(),
    val rules: Rules = Rules()
) {
    data class Architecture(
        val maxActivityLines: Int = 400,
        val enabled: Boolean = true,
        val defaultPattern: ArchitecturePattern = ArchitecturePattern.MVVM,
        val cleanLayers: CleanLayers = CleanLayers()
    )

    data class CleanLayers(
        val uiPackages: List<String> = emptyList(),
        val domainPackages: List<String> = emptyList(),
        val dataPackages: List<String> = emptyList()
    )

    data class Security(
        val failOnHardcodedSecrets: Boolean = true,
        val checkSharedPrefs: Boolean = true,
        val logging: Logging = Logging()
    )

    data class Logging(
        val allowDirectAndroidLog: Boolean = false,
        val allowedLogWrappers: List<String> = emptyList(),
        val sensitiveKeywords: List<String> = listOf(
            "token", "password", "secret", "authorization",
            "auth", "email", "phone", "ssn", "userId"
        )
    )

    data class Quality(
        val maxFunctionLines: Int = 40,
        val maxComplexity: Int = 10,
        val enabled: Boolean = true
    )

    data class Rules(
        val A1_GodActivity: Boolean = true,
        val S1_HardcodedSecrets: Boolean = true,
        val S2_InsecurePrefs: Boolean = true,
        val S3_ExportedComponent: Boolean = true
    )
}
