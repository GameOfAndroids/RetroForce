package com.tm78775.retroforcetest

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.tm78775.retroforce.RetroConfig
import com.tm78775.retroforce.RetroForce
import com.tm78775.retroforce.RetroForceActivity
import com.tm78775.retroforce.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : RetroForceActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}