package com.tm78775.retroforcetest

import android.app.Application
import android.util.Log

class RetroTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initRetroForce()
    }

    private fun initRetroForce() {
        Log.d(this.javaClass.simpleName, "Initializing RetroForce!")
//        RetroConfig.configureServer(
//
//        )
    }

}