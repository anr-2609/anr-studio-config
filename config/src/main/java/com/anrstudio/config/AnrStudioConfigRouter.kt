package com.anrstudio.config

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.anrstudio.config.ui.DeveloperChecklistActivity

object AnrStudioConfigRouter {

    fun openDeveloperChecklist(fromActivity: Activity) {
        fromActivity.startActivity(Intent(fromActivity, DeveloperChecklistActivity::class.java))
    }

    fun openDeveloperChecklist(context: Context) {
        val intent = Intent(context, DeveloperChecklistActivity::class.java)
        if (context !is Activity) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }
}
