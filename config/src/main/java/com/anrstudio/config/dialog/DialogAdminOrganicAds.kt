package com.anrstudio.config.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import com.anrstudio.ads.util.SharePreferenceUtils
import com.anrstudio.config.AnrStudioConfigRouter
import com.anrstudio.config.R
import com.anrstudio.config.ui.AnrStudioConfigTheme
import com.anrstudio.config.databinding.DialogAdminOrganicAdsBinding
import com.anrstudio.config.utils.click

object DialogAdminOrganicAds {

    private var onAdminAdToggleListener: ((Boolean) -> Unit)? = null

    fun setOnAdminAdToggleListener(listener: ((Boolean) -> Unit)?) {
        onAdminAdToggleListener = listener
    }

    fun show(context: Context) {
        val activity = context.findActivity() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val inflater = LayoutInflater.from(AnrStudioConfigTheme.wrap(activity))
        val binding = DialogAdminOrganicAdsBinding.inflate(inflater)
        val dialog = AppCompatDialog(activity, R.style.AnrStudioConfigDialog)
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        AnrStudioConfigTheme.applyLightWindow(dialog.window)
        dialog.setCancelable(false)

        syncSwitchFromPreference(context, binding)
        val initialUnlimitedAdsEnabled = binding.switchUnlimitedAds.isChecked

        binding.btnChecklist.click {
            dialog.dismiss()
            AnrStudioConfigRouter.openDeveloperChecklist(activity)
        }

        binding.btnApply.click {
            val unlimitedAdsEnabled = binding.switchUnlimitedAds.isChecked
            SharePreferenceUtils.setIsOrganic(context, !unlimitedAdsEnabled)
            if (unlimitedAdsEnabled != initialUnlimitedAdsEnabled) {
                onAdminAdToggleListener?.invoke(unlimitedAdsEnabled)
            }

            val messageRes = if (unlimitedAdsEnabled) {
                R.string.txt_admin_ads_unlimited_enabled
            } else {
                R.string.txt_admin_ads_unlimited_disabled
            }
            Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }

        dialog.setContentView(binding.root)
        dialog.show()
    }

    private fun syncSwitchFromPreference(
        context: Context,
        binding: DialogAdminOrganicAdsBinding
    ) {
        val isOrganic = SharePreferenceUtils.getIsOrganic(context)
        binding.switchUnlimitedAds.isChecked = !isOrganic
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}
