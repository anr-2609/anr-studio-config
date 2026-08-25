package com.anrstudio.config.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.MotionEvent
import android.view.View
import com.anrstudio.config.dialog.DialogAdminOrganicAds

private tailrec fun Context.findHostContext(): Context? = when (this) {
    is Activity -> if (isFinishing || isDestroyed) null else this
    is ContextWrapper -> baseContext.findHostContext()
    else -> null
}

@SuppressLint("ClickableViewAccessibility")
fun View.setOnAdminAdToggleListener(
    targetTapCount: Int = 10,
    tapTimeout: Long = 4000L,
    onAdminAdToggled: ((Boolean) -> Unit)? = null
) {
    var tapCount = 0
    val handler = Handler(Looper.getMainLooper())

    val resetTapCountRunnable = Runnable {
        tapCount = 0
    }

    this.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_DOWN) {
            handler.removeCallbacks(resetTapCountRunnable)
            tapCount++

            if (tapCount == targetTapCount) {
                tapCount = 0
                DialogAdminOrganicAds.setOnAdminAdToggleListener(onAdminAdToggled)
                val hostContext = context.findHostContext()
                if (hostContext != null) {
                    DialogAdminOrganicAds.show(hostContext)
                }
            } else {
                handler.postDelayed(resetTapCountRunnable, tapTimeout)
            }
        }
        false
    }
}

internal fun View.click(action: (view: View?) -> Unit) {
    var lastClickTime = 0L
    setOnClickListener {
        if (SystemClock.elapsedRealtime() - lastClickTime < 300L) return@setOnClickListener
        lastClickTime = SystemClock.elapsedRealtime()
        action(it)
    }
}
