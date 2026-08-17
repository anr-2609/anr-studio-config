package com.fireants.devconfig.dialog

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Color
import android.view.LayoutInflater
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatDialog
import androidx.core.graphics.drawable.toDrawable
import com.fireants.adsdk.util.SharePreferenceUtils
import com.fireants.devconfig.FireAntsDevConfigRouter
import com.fireants.devconfig.R
import com.fireants.devconfig.ui.FireAntsDevConfigTheme
import com.fireants.devconfig.databinding.DialogAdminOrganicAdsBinding
import com.fireants.devconfig.utils.click

object DialogAdminOrganicAds {

    private var onAdminAdToggleListener: ((Boolean) -> Unit)? = null

    fun setOnAdminAdToggleListener(listener: ((Boolean) -> Unit)?) {
        onAdminAdToggleListener = listener
    }

    fun show(context: Context) {
        val activity = context.findActivity() ?: return
        if (activity.isFinishing || activity.isDestroyed) return

        val inflater = LayoutInflater.from(FireAntsDevConfigTheme.wrap(activity))
        val binding = DialogAdminOrganicAdsBinding.inflate(inflater)
        val dialog = AppCompatDialog(activity, R.style.FireAntsDevConfigDialog)
        dialog.supportRequestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
        FireAntsDevConfigTheme.applyLightWindow(dialog.window)
        dialog.setCancelable(false)

        syncSwitchFromPreference(context, binding)
        val initialUnlimitedAdsEnabled = binding.switchUnlimitedAds.isChecked

        binding.btnChecklist.click {
            dialog.dismiss()
            FireAntsDevConfigRouter.openDeveloperChecklist(activity)
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
