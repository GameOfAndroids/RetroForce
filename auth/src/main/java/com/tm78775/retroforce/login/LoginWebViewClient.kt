package com.tm78775.retroforce.login

import android.graphics.Bitmap
import android.webkit.*
import com.tm78775.retroforce.model.AuthToken
import com.tm78775.retroforce.model.AuthTokenParser
import com.tm78775.retroforce.model.Server

internal class LoginWebViewClient(
    private val server: Server,
    private val tokenParser: AuthTokenParser,
    private val onTokenParsed: (AuthToken) -> Unit
) : WebViewClient() {

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

        url?.let {
            if(tokenParser.isRedirectUriDetected(server, it)) {
                val token = tokenParser.parseAuthToken(it)
                onTokenParsed(token)
                view?.stopLoading()
            }
        }
    }

}