package com.dgreenhalgh.android.largemargebackground

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class LargeMargeApplication: Application() {

    override fun onCreate() {
        initCalligraphy()
    }

    private fun initCalligraphy() {
        // TODO: Add custom fontPath
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setFontAttrId(R.attr.fontPath)
                .build())
    }
}