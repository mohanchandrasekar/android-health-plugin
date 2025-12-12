package com.android.healthcheck

import java.io.File

class AndroidHealthAnalyzer(
    private val config: HealthConfig
) {
    private val secretScanner =
        if (config.security.failOnHardcodedSecrets && config.rules.S1_HardcodedSecrets)
            SecretScanner()
        else null

    private val prefsScanner =
        if (config.security.checkSharedPrefs && config.rules.S2_InsecurePrefs)
            InsecurePrefsScanner()
        else null

    private val godActivityScanner =
        if (config.architecture.enabled && config.rules.A1_GodActivity)
            GodActivityScanner(config.architecture.maxActivityLines)
        else null

    private val manifestScanner =
        if (config.rules.S3_ExportedComponent)
            ManifestScanner()
        else null

    private val mvvmScanner: MvvmScanner? =
        if (config.architecture.enabled &&
            config.architecture.defaultPattern == ArchitecturePattern.MVVM
        ) MvvmScanner(config) else null

    private val cleanArchScanner: CleanArchScanner? =
        if (config.architecture.enabled) CleanArchScanner(config) else null

    private val loggingScanner = LoggingScanner(config)
    private val qualityScanner: QualityScanner? =
        if (config.quality.enabled) QualityScanner(config) else null

    private val staticAnalysisScanner = StaticAnalysisScanner()
    private val duplicateCodeScanner = DuplicateCodeScanner()
    private val testingScanner = TestingScanner()

    fun analyze(rootDir: File): List<Finding> {
        val findings = mutableListOf<Finding>()

        rootDir.walkTopDown()
            .filter { it.isFile && (it.extension == "kt" || it.extension == "java") }
            .forEach { file ->
                godActivityScanner?.scan(file)?.let { findings += it }
                secretScanner?.scan(file)?.let { findings += it }
                prefsScanner?.scan(file)?.let { findings += it }
                mvvmScanner?.scan(file)?.let { findings += it }
                cleanArchScanner?.scan(file)?.let { findings += it }
                findings += loggingScanner.scan(file)
                qualityScanner?.scan(file)?.let { findings += it }
            }

        rootDir.walkTopDown()
            .filter { it.isFile && it.name == "AndroidManifest.xml" }
            .forEach { file ->
                manifestScanner?.scan(file)?.let { findings += it }
            }

        findings += staticAnalysisScanner.scanProject(rootDir)
        findings += duplicateCodeScanner.scanProject(rootDir)
        findings += testingScanner.scanProject(rootDir)

        return findings
    }
}
