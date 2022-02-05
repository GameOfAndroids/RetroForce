package com.tm78775.retroforcetest

import android.os.Bundle
import com.tm78775.retroforce.RetroConfig
import com.tm78775.retroforce.RetroForceActivity
import com.tm78775.retroforce.model.Server
import com.tm78775.retroforce.model.ServerEnvironment

class MainActivity : RetroForceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TODO("Call RetroConfig.configureServer()")
//        RetroConfig.configureServer(
//
//        )
        setContentView(R.layout.activity_main)
    }

}