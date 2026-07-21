package com.fireants.devconfig

import android.content.Context
import android.os.Build

object FireAntsDevConfig {

    @Volatile
    private var appConfig: FireAntsDevConfigAppConfig? = null

    fun init(
        context: Context,
        nkhStudioVersion: String,
        playServicesAdsVersion: String,
        gdprModuleVersion: String
    ) {
        val packageInfo = getPackageInfoCompat(context)
        val versionName = packageInfo?.versionName?.takeIf { it.isNotBlank() } ?: "N/A"

        appConfig = FireAntsDevConfigAppConfig(
            isDebugBuild = (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0,
            versionName = versionName,
            adjustToken = context.getHostString("adjust_token"),
            facebookAppId = context.getHostString("facebook_app_id"),
            facebookClientToken = context.getHostString("facebook_client_token"),
            tiktokEventToken = context.getHostString("event_token"),
            nkhStudioVersion = nkhStudioVersion,
            playServicesAdsVersion = playServicesAdsVersion,
            gdprModuleVersion = gdprModuleVersion
        )
    }

    internal fun requireAppConfig(): FireAntsDevConfigAppConfig {
        return appConfig
            ?: error("FireAntsDevConfig.init() must be called before using developer settings.")
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
