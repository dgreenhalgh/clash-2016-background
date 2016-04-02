package com.dgreenhalgh.android.largemargebackground

import android.app.Application
import uk.co.chrisjenx.calligraphy.CalligraphyConfig

class LargeMargeApplication: Application() {

    private val DEFAULT_FONT_PATH = "fonts/NotoSerif-Regular.ttf"

    override fun onCreate() {
        initCalligraphy()
    }

    private fun initCalligraphy() {
        CalligraphyConfig.initDefault(CalligraphyConfig.Builder()
                .setDefaultFontPath(DEFAULT_FONT_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build())
    }
}