package com.fireants.devconfig.ui

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class FireAntsDevConfigBaseActivity<VB : ViewDataBinding> : AppCompatActivity() {

    protected lateinit var binding: VB

    protected abstract fun getLayoutId(): Int

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(FireAntsDevConfigTheme.wrapActivityBase(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.localNightMode = AppCompatDelegate.MODE_NIGHT_NO
        super.onCreate(savedInstanceState)
        FireAntsDevConfigTheme.applyLightWindow(window)
        binding = DataBindingUtil.setContentView(this, getLayoutId())
        binding.lifecycleOwner = this
        onBind()
    }

    protected open fun onBind() {}
}
