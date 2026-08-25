package com.anrstudio.config.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.initialization.AdapterStatus
import com.google.android.gms.ads.initialization.InitializationStatus
import com.anrstudio.config.R
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object MediationCheckUtils {

    data class MediationNetworkStatus(
        val networkName: String,
        val adapterClassName: String,
        val integrationState: MediationIntegrationState,
        val description: String,
        val latencyMs: Long = 0L,
        val isSdkOnClasspath: Boolean = false
    )

    enum class MediationIntegrationState {
        READY,
        NOT_READY,
        NOT_INTEGRATED,
        SDK_ONLY
    }

    private data class MediationNetworkProbe(
        val displayName: String,
        val adapterKeywords: List<String>,
        val sdkClassNames: List<String> = emptyList()
    )

    private val knownNetworks = listOf(
        MediationNetworkProbe(
            displayName = "AdMob (Google)",
            adapterKeywords = listOf("admob", "AdMobAdapter", "GoogleMobileAds")
        ),
        MediationNetworkProbe(
            displayName = "Meta Audience Network",
            adapterKeywords = listOf("facebook", "Facebook"),
            sdkClassNames = listOf("com.facebook.ads.AudienceNetworkAds")
        ),
        MediationNetworkProbe(
            displayName = "AppLovin",
            adapterKeywords = listOf("applovin", "AppLovin"),
            sdkClassNames = listOf("com.applovin.sdk.AppLovinSdk")
        ),
        MediationNetworkProbe(
            displayName = "Unity Ads",
            adapterKeywords = listOf("unity", "Unity"),
            sdkClassNames = listOf("com.unity3d.ads.UnityAds")
        ),
        MediationNetworkProbe(
            displayName = "ironSource",
            adapterKeywords = listOf("ironsource", "IronSource"),
            sdkClassNames = listOf("com.ironsource.mediationsdk.IronSource")
        ),
        MediationNetworkProbe(
            displayName = "Mintegral",
            adapterKeywords = listOf("mintegral", "Mintegral", "mbridge"),
            sdkClassNames = listOf("com.mbridge.msdk.MBridgeSDK")
        ),
        MediationNetworkProbe(
            displayName = "Pangle",
            adapterKeywords = listOf("pangle", "Pangle", "bytedance"),
            sdkClassNames = listOf("com.bytedance.sdk.openadsdk.TTAdSdk")
        ),
        MediationNetworkProbe(
            displayName = "Vungle",
            adapterKeywords = listOf("vungle", "Vungle"),
            sdkClassNames = listOf("com.vungle.warren.Vungle")
        ),
        MediationNetworkProbe(
            displayName = "Chartboost",
            adapterKeywords = listOf("chartboost", "Chartboost"),
            sdkClassNames = listOf("com.chartboost.sdk.Chartboost")
        ),
        MediationNetworkProbe(
            displayName = "InMobi",
            adapterKeywords = listOf("inmobi", "InMobi"),
            sdkClassNames = listOf("com.inmobi.sdk.InMobiSdk")
        ),
        MediationNetworkProbe(
            displayName = "DT Exchange (Fyber)",
            adapterKeywords = listOf("fyber", "inneractive", "Fyber")
        ),
        MediationNetworkProbe(
            displayName = "AdColony",
            adapterKeywords = listOf("adcolony", "AdColony"),
            sdkClassNames = listOf("com.adcolony.sdk.AdColony")
        )
    )

    fun fetchMediationStatuses(
        context: Context,
        onResult: (List<MediationNetworkStatus>) -> Unit
    ) {
        runOnMainThread {
            MobileAds.initialize(context) { initializationStatus ->
                onResult(buildStatuses(initializationStatus))
            }
        }
    }

    fun fetchMediationStatusesBlocking(
        context: Context,
        timeoutMs: Long = 8_000L
    ): List<MediationNetworkStatus> {
        val latch = CountDownLatch(1)
        var result = emptyList<MediationNetworkStatus>()
        fetchMediationStatuses(context) { statuses ->
            result = statuses
            latch.countDown()
        }
        latch.await(timeoutMs, TimeUnit.MILLISECONDS)
        return result
    }

    fun getStatusLabel(context: Context, state: MediationIntegrationState): String {
        return when (state) {
            MediationIntegrationState.READY ->
                context.getString(R.string.developer_checklist_mediation_ready)
            MediationIntegrationState.NOT_READY ->
                context.getString(R.string.developer_checklist_mediation_not_ready)
            MediationIntegrationState.SDK_ONLY ->
                context.getString(R.string.developer_checklist_mediation_sdk_only)
            MediationIntegrationState.NOT_INTEGRATED ->
                context.getString(R.string.developer_checklist_mediation_not_integrated)
        }
    }

    private fun buildStatuses(initializationStatus: InitializationStatus): List<MediationNetworkStatus> {
        val adapterMap = initializationStatus.adapterStatusMap
        val matchedKeys = mutableSetOf<String>()
        val results = mutableListOf<MediationNetworkStatus>()

        knownNetworks.forEach { probe ->
            val matchedEntry = adapterMap.entries.firstOrNull { (adapterClass, _) ->
                probe.adapterKeywords.any { keyword ->
                    adapterClass.contains(keyword, ignoreCase = true)
                }
            }
            if (matchedEntry != null) {
                matchedKeys += matchedEntry.key
            }
            results += probe.toStatus(matchedEntry)
        }

        adapterMap.forEach { (adapterClass, status) ->
            if (adapterClass in matchedKeys) return@forEach
            if (adapterClass.contains("MobileAds", ignoreCase = true)) return@forEach
            matchedKeys += adapterClass
            results += MediationNetworkStatus(
                networkName = resolveDisplayName(adapterClass),
                adapterClassName = simplifyClassName(adapterClass),
                integrationState = mapInitializationState(status),
                description = status.description.orEmpty(),
                latencyMs = status.latency.toLong(),
                isSdkOnClasspath = true
            )
        }

        return results.sortedWith(
            compareBy<MediationNetworkStatus> { it.integrationState.sortOrder() }
                .thenBy { it.networkName.lowercase(Locale.getDefault()) }
        )
    }

    private fun MediationNetworkProbe.toStatus(
        matchedEntry: Map.Entry<String, AdapterStatus>?
    ): MediationNetworkStatus {
        val sdkPresent = sdkClassNames.any { isClassPresent(it) }
        if (matchedEntry != null) {
            val status = matchedEntry.value
            return MediationNetworkStatus(
                networkName = displayName,
                adapterClassName = simplifyClassName(matchedEntry.key),
                integrationState = mapInitializationState(status),
                description = status.description.orEmpty(),
                latencyMs = status.latency.toLong(),
                isSdkOnClasspath = true
            )
        }
        return MediationNetworkStatus(
            networkName = displayName,
            adapterClassName = "—",
            integrationState = if (sdkPresent) {
                MediationIntegrationState.SDK_ONLY
            } else {
                MediationIntegrationState.NOT_INTEGRATED
            },
            description = if (sdkPresent) {
                "SDK detected, mediation adapter not found"
            } else {
                "Adapter/SDK not found in app"
            },
            isSdkOnClasspath = sdkPresent
        )
    }

    private fun mapInitializationState(status: AdapterStatus): MediationIntegrationState {
        return when (status.initializationState) {
            AdapterStatus.State.READY -> MediationIntegrationState.READY
            AdapterStatus.State.NOT_READY -> MediationIntegrationState.NOT_READY
            else -> MediationIntegrationState.NOT_READY
        }
    }

    private fun MediationIntegrationState.sortOrder(): Int = when (this) {
        MediationIntegrationState.READY -> 0
        MediationIntegrationState.NOT_READY -> 1
        MediationIntegrationState.SDK_ONLY -> 2
        MediationIntegrationState.NOT_INTEGRATED -> 3
    }

    private fun isClassPresent(className: String): Boolean {
        return try {
            Class.forName(className)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun simplifyClassName(fullClassName: String): String {
        return fullClassName.substringAfterLast('.')
    }

    private fun resolveDisplayName(adapterClass: String): String {
        val simple = simplifyClassName(adapterClass)
        return simple
            .replace("MediationAdapter", "", ignoreCase = true)
            .replace("Adapter", "", ignoreCase = true)
            .ifBlank { simple }
    }

    private fun runOnMainThread(block: () -> Unit) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            block()
        } else {
            Handler(Looper.getMainLooper()).post(block)
        }
    }
}
