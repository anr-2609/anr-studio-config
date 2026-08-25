package com.anrstudio.config.ui

import android.content.Context
import android.content.pm.PackageManager
import com.anrstudio.config.ANRConfig
import com.anrstudio.config.R
import com.anrstudio.config.utils.MediationCheckUtils
import org.json.JSONObject

object DeveloperChecklistRepository {

    private const val RELEASE_AD_CONFIG = "ad_config.json"

    fun buildChecklistItems(
        context: Context,
        mediationStatuses: List<MediationCheckUtils.MediationNetworkStatus>? = null
    ): List<DeveloperChecklistItem> {
        val items = mutableListOf<DeveloperChecklistItem>()

        addSection(
            items = items,
            sectionId = DeveloperChecklistSectionId.SDK,
            title = context.getString(R.string.developer_checklist_section_sdk),
            children = listOf(
                infoRow(
                    context.getString(R.string.developer_checklist_anrstudio_ads),
                    ANRConfig.requireAppConfig().anrstudioAdsVersion
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_play_services_ads),
                    ANRConfig.requireAppConfig().playServicesAdsVersion
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_gdpr_module),
                    ANRConfig.requireAppConfig().gdprModuleVersion
                )
            )
        )

        val appConfig = ANRConfig.requireAppConfig()
        addSection(
            items = items,
            sectionId = DeveloperChecklistSectionId.TRACKING,
            title = context.getString(R.string.developer_checklist_section_tracking),
            children = listOf(
                infoRow(
                    context.getString(R.string.developer_checklist_appsflyer_key),
                    appConfig.appsFlyerKey
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_facebook_app_id),
                    appConfig.facebookAppId
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_facebook_client_token),
                    appConfig.facebookClientToken
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_tiktok_event_token),
                    appConfig.tiktokEventToken
                ),
                infoRow(
                    context.getString(R.string.developer_checklist_admob_app_id),
                    getAdMobAppId(context)
                )
            )
        )

        val mediationChildren = buildMediationChildren(context, mediationStatuses)
        addSection(
            items = items,
            sectionId = DeveloperChecklistSectionId.MEDIATION,
            title = context.getString(R.string.developer_checklist_section_mediation),
            children = mediationChildren
        )

        val releasePlacements = loadAdPlacements(context, RELEASE_AD_CONFIG)
        addSection(
            items = items,
            sectionId = DeveloperChecklistSectionId.AD_RELEASE,
            title = context.getString(
                R.string.developer_checklist_section_ad_release,
                RELEASE_AD_CONFIG
            ),
            children = releasePlacements
        )

        return items
    }

    fun buildMediationChildren(
        context: Context,
        statuses: List<MediationCheckUtils.MediationNetworkStatus>?
    ): List<DeveloperChecklistItem> {
        if (statuses == null) {
            return listOf(
                infoRow(
                    context.getString(R.string.developer_checklist_mediation_col_status),
                    context.getString(R.string.developer_checklist_mediation_loading)
                )
            )
        }
        if (statuses.isEmpty()) {
            return listOf(
                infoRow(
                    context.getString(R.string.developer_checklist_mediation_col_status),
                    context.getString(R.string.developer_checklist_mediation_empty)
                )
            )
        }

        val rows = mutableListOf<DeveloperChecklistItem>(DeveloperChecklistItem.MediationTableHeader)
        statuses.forEach { status ->
            val detail = buildString {
                if (status.description.isNotBlank()) {
                    append(status.description)
                }
                if (status.latencyMs > 0) {
                    if (isNotEmpty()) append(" · ")
                    append(
                        context.getString(
                            R.string.developer_checklist_mediation_latency,
                            status.latencyMs
                        )
                    )
                }
            }
            rows += DeveloperChecklistItem.MediationRow(
                networkName = status.networkName,
                statusLabel = MediationCheckUtils.getStatusLabel(context, status.integrationState),
                integrationState = status.integrationState,
                adapterClassName = status.adapterClassName,
                detail = detail
            )
        }
        return rows
    }

    private fun addSection(
        items: MutableList<DeveloperChecklistItem>,
        sectionId: String,
        title: String,
        children: List<DeveloperChecklistItem>
    ) {
        items += DeveloperChecklistItem.Section(
            sectionId = sectionId,
            title = title,
            childCount = children.size
        )
        items += children
    }

    private fun infoRow(label: String, value: String) =
        DeveloperChecklistItem.InfoRow(label, value)

    private fun loadAdPlacements(context: Context, fileName: String): List<DeveloperChecklistItem> {
        return try {
            val jsonText = context.assets.open(fileName).bufferedReader().use { it.readText() }
            val root = JSONObject(jsonText)
            root.keys().asSequence()
                .map { key -> key to root.getJSONObject(key) }
                .sortedBy { it.first }
                .map { (placement, unit) ->
                    DeveloperChecklistItem.AdPlacement(
                        placement = placement,
                        adId = unit.optString("id", "—"),
                        isEnabled = unit.optBoolean("isEnable", false)
                    )
                }
                .toList()
        } catch (e: Exception) {
            listOf(
                DeveloperChecklistItem.InfoRow(
                    label = fileName,
                    value = context.getString(
                        R.string.developer_checklist_load_error,
                        e.message ?: "unknown"
                    )
                )
            )
        }
    }

    private fun getAdMobAppId(context: Context): String {
        return try {
            val appInfo = context.packageManager.getApplicationInfo(
                context.packageName,
                PackageManager.GET_META_DATA
            )
            appInfo.metaData?.getString("com.google.android.gms.ads.APPLICATION_ID")
                ?: context.getString(R.string.developer_checklist_na)
        } catch (_: Exception) {
            context.getString(R.string.developer_checklist_na)
        }
    }
}
