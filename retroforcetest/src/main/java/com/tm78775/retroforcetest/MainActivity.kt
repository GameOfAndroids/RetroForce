package com.tm78775.retroforcetest

import android.os.Bundle
import com.tm78775.retroforce.RetroForceActivity
import com.tm78775.retroforce.model.Server
import com.tm78775.retroforce.model.ServerEnvironment

class MainActivity : RetroForceActivity() {

    override fun getServer() = TODO("Enter Server Info Here.")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}