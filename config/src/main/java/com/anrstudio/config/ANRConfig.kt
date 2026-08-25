package com.anrstudio.config

import android.content.Context
import android.os.Build

object AnrStudioConfig {

    @Volatile
    private var appConfig: AnrStudioConfigAppConfig? = null

    fun init(
        context: Context,
        anrstudioAdsVersion: String,
        playServicesAdsVersion: String,
        gdprModuleVersion: String
    ) {
        val packageInfo = getPackageInfoCompat(context)
        val versionName = packageInfo?.versionName?.takeIf { it.isNotBlank() } ?: "N/A"

        appConfig = AnrStudioConfigAppConfig(
            isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            versionName = versionName,
            appsFlyerKey = context.getHostString("appsflyer_key"),
            facebookAppId = context.getHostString("facebook_app_id"),
            facebookClientToken = context.getHostString("facebook_client_token"),
            tiktokEventToken = context.getHostString("event_token"),
            anrstudioAdsVersion = anrstudioAdsVersion,
            playServicesAdsVersion = playServicesAdsVersion,
            gdprModuleVersion = gdprModuleVersion
        )
    }

    internal fun requireAppConfig(): AnrStudioConfigAppConfig {
        return appConfig
            ?: error("AnrStudioConfig.init() must be called before using developer settings.")
    }

    private fun Context.getHostString(name: String): String {
        val id = resources.getIdentifier(name, "string", packageName)
        return if (id != 0) getString(id) else "N/A"
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfoCompat(context: Context): android.content.pm.PackageInfo? {
        return try {
            val pm = context.packageManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(context.packageName, android.content.pm.PackageManager.PackageInfoFlags.of(0))
            } else {
                pm.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }
    }
}
