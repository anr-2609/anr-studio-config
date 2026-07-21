package com.fireants.devconfig.ui

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.Window
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import com.fireants.devconfig.R
internal object FireAntsDevConfigTheme {

    fun wrap(
        context: Context,
        @StyleRes themeRes: Int = R.style.ThemeFireAntsDevConfig
    ): Context = ContextThemeWrapper(context, themeRes)

    fun wrapActivityBase(
        context: Context,
        @StyleRes themeRes: Int = R.style.ThemeFireAntsDevConfig
    ): Context {
        val lightConfiguration = Configuration(context.resources.configuration).apply {
            uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or Configuration.UI_MODE_NIGHT_NO
        }
        val lightContext = context.createConfigurationContext(lightConfiguration)
        return ContextThemeWrapper(lightContext, themeRes)
    }

    fun applyLightWindow(window: Window?) {
        window ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.decorView.isForceDarkAllowed = false
        }
    }
}
