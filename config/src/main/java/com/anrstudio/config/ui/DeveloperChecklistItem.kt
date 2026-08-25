package com.anrstudio.config.ui

import com.anrstudio.config.utils.MediationCheckUtils.MediationIntegrationState

sealed class DeveloperChecklistItem {
    data class Section(
        val sectionId: String,
        val title: String,
        val childCount: Int
    ) : DeveloperChecklistItem()

    data class InfoRow(val label: String, val value: String) : DeveloperChecklistItem()

    data class AdPlacement(
        val placement: String,
        val adId: String,
        val isEnabled: Boolean
    ) : DeveloperChecklistItem()

    data object MediationTableHeader : DeveloperChecklistItem()

    data class MediationRow(
        val networkName: String,
        val statusLabel: String,
        val integrationState: MediationIntegrationState,
        val adapterClassName: String,
        val detail: String
    ) : DeveloperChecklistItem()
}

object DeveloperChecklistSectionId {
    const val SDK = "section_sdk"
    const val TRACKING = "section_tracking"
    const val MEDIATION = "section_mediation"
    const val AD_RELEASE = "section_ad_release"
}
