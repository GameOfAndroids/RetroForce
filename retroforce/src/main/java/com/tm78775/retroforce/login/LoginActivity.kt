package com.tm78775.retroforce.login

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.tm78775.retroforce.viewmodel.CommunityAuthViewModel
import com.tm78775.retroforce.R
import com.tm78775.retroforce.model.AuthTokenParser
import com.tm78775.retroforce.model.ConnectedApp
import com.tm78775.retroforce.theme.RetroForceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LoginActivity : ComponentActivity() {

    private val viewModel: CommunityAuthViewModel by lazy {
        CommunityAuthViewModel(application)
    }
    private lateinit var loginWebViewClient: LoginWebViewClient
    private lateinit var connectedApp: ConnectedApp
    private lateinit var tokenParser: AuthTokenParser

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connectedApp = intent?.extras?.getSerializable("server") as ConnectedApp
        tokenParser = intent?.extras?.getSerializable("token_parser") as AuthTokenParser

        loginWebViewClient = LoginWebViewClient(connectedApp, tokenParser) { parsedToken ->
            lifecycleScope.launch(Dispatchers.IO) {
                setResult(RESULT_OK, Intent().putExtra("token", parsedToken))
                lifecycleScope.launch(Dispatchers.Main) {
                    delay(500)
                    finish()
                    overridePendingTransition(R.anim.stay, R.anim.slide_down)
                }
            }
        }

        setContent {
            RetroForceTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    WebViewPage(
                        loginWebViewClient,
                        viewModel.generateAuthEndpoint(connectedApp, provideDeviceId())
                    )
                }
            }
        }
    }

    override fun onBackPressed() {
        Log.d(this::class.simpleName, "Back is disabled when login screen is presented.")
    }

    @SuppressLint("HardwareIds")
    private fun provideDeviceId(): String {
        return Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
    }

}

@SuppressLint("SetJavaScriptEnabled")
@Composable
internal fun WebViewPage(client: LoginWebViewClient, url: String) {
    AndroidView(
        factory = {
            WebView(it).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.apply {
                    useWideViewPort = true
                    layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
                    javaScriptEnabled = true
                    javaScriptCanOpenWindowsAutomatically = true
                    databaseEnabled = true
                    domStorageEnabled = true
                }

                webViewClient = client
                loadUrl(url)
            }
        }
    )
}