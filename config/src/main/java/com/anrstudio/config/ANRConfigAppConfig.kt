package com.anrstudio.config

data class ANRConfigAppConfig(
    val isDebugBuild: Boolean,
    val versionName: String,
    val appsFlyerKey: String,
    val facebookAppId: String,
    val facebookClientToken: String,
    val tiktokEventToken: String,
    val anrstudioAdsVersion: String,
    val playServicesAdsVersion: String,
    val gdprModuleVersion: String
)
