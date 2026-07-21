package com.fireants.devconfig

data class FireAntsDevConfigAppConfig(
    val isDebugBuild: Boolean,
    val versionName: String,
    val adjustToken: String,
    val facebookAppId: String,
    val facebookClientToken: String,
    val tiktokEventToken: String,
    val nkhStudioVersion: String,
    val playServicesAdsVersion: String,
    val gdprModuleVersion: String
)
