package com.anrstudio.config.ui

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.anrstudio.config.AnrStudioConfig
import com.anrstudio.config.R
import com.anrstudio.config.databinding.ActivityDeveloperChecklistBinding
import com.anrstudio.config.utils.MediationCheckUtils
import com.anrstudio.config.utils.click

class DeveloperChecklistActivity : AnrStudioConfigBaseActivity<ActivityDeveloperChecklistBinding>() {

    private var checklistAdapter: DeveloperChecklistAdapter? = null

    override fun getLayoutId(): Int = R.layout.activity_developer_checklist

    override fun onBind() {
        bindBuildVariantBadge()
        checklistAdapter = DeveloperChecklistAdapter(
            DeveloperChecklistRepository.buildChecklistItems(this, mediationStatuses = null)
        )
        binding.rvChecklist.apply {
            layoutManager = LinearLayoutManager(this@DeveloperChecklistActivity)
            adapter = checklistAdapter
            setHasFixedSize(true)
        }
        binding.imvBack.click { finish() }
        loadMediationStatuses()
    }

    private fun loadMediationStatuses() {
        MediationCheckUtils.fetchMediationStatuses(this) { statuses ->
            if (isFinishing || isDestroyed) return@fetchMediationStatuses
            checklistAdapter?.replaceItems(
                DeveloperChecklistRepository.buildChecklistItems(
                    context = this,
                    mediationStatuses = statuses
                )
            )
        }
    }

    private fun bindBuildVariantBadge() {
        val config = AnrStudioConfig.requireAppConfig()
        binding.tvBuildVariant.apply {
            text = if (config.isDebugBuild) {
                getString(R.string.developer_checklist_build_debug, config.versionName)
            } else {
                getString(R.string.developer_checklist_build_release, config.versionName)
            }
            background = ContextCompat.getDrawable(
                this@DeveloperChecklistActivity,
                if (config.isDebugBuild) {
                    R.drawable.bg_build_badge_debug
                } else {
                    R.drawable.bg_build_badge_release
                }
            )
        }
    }
}
