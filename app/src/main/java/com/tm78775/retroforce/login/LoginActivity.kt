package com.tm78775.retroforce.login

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import com.tm78775.retroforce.R
import com.tm78775.retroforce.RetroForceActivity
import com.tm78775.retroforce.model.AuthTokenParser
import com.tm78775.retroforce.model.Server
import com.tm78775.retroforce.theme.RetroForceTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

internal class LoginActivity : RetroForceActivity() {

    private val viewModel: LoginViewModel by viewModels()
    private lateinit var loginWebViewClient: LoginWebViewClient
    private lateinit var server: Server
    private lateinit var tokenParser: AuthTokenParser

    override fun getServer() = server
    override fun getAuthTokenParser() = tokenParser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        server = intent?.extras?.getSerializable("server") as Server
        tokenParser = intent?.extras?.getSerializable("token_parser") as AuthTokenParser

        loginWebViewClient = LoginWebViewClient(getServer(), getAuthTokenParser()) { parsedToken ->
            lifecycleScope.launch(Dispatchers.IO) {
                saveAuthToken(parsedToken)
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
                        viewModel.generateAuthEndpoint(getServer(), provideDeviceId())
                    )
                }
            }
        }
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