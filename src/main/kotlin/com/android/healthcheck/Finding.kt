package com.android.healthcheck

data class Finding(
    val ruleId: String,
    val category: String,
    val severity: String,
    val file: String,
    val message: String
)
