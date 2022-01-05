package com.tm78775.retroforce.model

import android.webkit.URLUtil
import java.nio.charset.StandardCharsets

class SalesforceCommunityTokenParser : AuthTokenParser {
    override fun isRedirectUriDetected(server: Server, url: String): Boolean {
        return url.startsWith(server.redirectUri)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <AuthToken> parseAuthToken(url: String): AuthToken {
        val tokenVars = hashMapOf<String, String>()
        val redirectUrl = url.substringBefore("#")
        url.substringAfter("#").also { authVars ->
            val keyValuePairStrings = authVars.split('&')
            keyValuePairStrings.forEach { pair ->
                val p = pair.split('=')
                tokenVars[convertHtmlString(p.first())] = convertHtmlString(p.last())
            }
        }

        return AuthToken(
            redirectUri = redirectUrl,
            accessToken = tokenVars["access_token"] ?: "",
            refreshToken = tokenVars["refresh_token"] ?: "",
            communityUrl = tokenVars["sfdc_community_url"] ?: "",
            communityId = tokenVars["sfdc_community_id"] ?: "",
            signature = tokenVars["signature"] ?: "",
            instanceUrl = tokenVars["instance_url"] ?: "",
            idUrl = tokenVars["id"] ?: "",
            tokenType = tokenVars["token_type"] ?: "",
            issuedAt = tokenVars["issued_at"] ?: "",
            scope = tokenVars["scope"]?.split('+') ?: listOf()
        ) as AuthToken
    }

    private fun convertHtmlString(html: String): String {
        val r: ByteArray = URLUtil.decode(html.toByteArray())
        return String(r, StandardCharsets.UTF_8)
    }

}