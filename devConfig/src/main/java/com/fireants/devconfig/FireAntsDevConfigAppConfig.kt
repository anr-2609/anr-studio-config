package com.fireants.devconfig

data class FireAntsDevConfigAppConfig(
    val isDebugBuild: Boolean,
    val versionName: String,
    val appsFlyerKey: String,
    val facebookAppId: String,
    val facebookClientToken: String,
    val tiktokEventToken: String,
    val fireantsAdsVersion: String,
    val playServicesAdsVersion: String,
    val gdprModuleVersion: String
)
