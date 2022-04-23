package com.tm78775.retroforce.service

import android.webkit.URLUtil
import com.tm78775.retroforce.model.*
import java.nio.charset.StandardCharsets

class SalesforceCommunityTokenParser : AuthTokenParser {

    override fun isRedirectUriDetected(connectedApp: ConnectedApp, url: String): Boolean {
        return url.startsWith(connectedApp.redirectUri)
    }

    override fun parseAuthToken(url: String): AuthToken {
        val tokenVars = hashMapOf<String, String>()
        val redirectUrl = url.substringBefore("#")
        url.substringAfter("#").also { authVars ->
            val keyValuePairStrings = authVars.split('&')
            keyValuePairStrings.forEach { pair ->
                val p = pair.split('=')
                tokenVars[convertHtmlString(p.first())] = convertHtmlString(p.last())
            }
        }

        return CommunityAuthToken(
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
        )
    }

    override fun parseRefreshToken(token: AuthToken, response: RefreshTokenResponse): AuthToken {
        return (token as CommunityAuthToken).apply {
            accessToken = response.accessToken
            tokenType = response.tokenType
            idUrl = response.id
            issuedAt = response.issuedAt
            instanceUrl = response.instanceUrl
            signature = response.signature
            scope = response.scope.split('+')
        }
    }

    private fun convertHtmlString(html: String): String {
        val r: ByteArray = URLUtil.decode(html.toByteArray())
        return String(r, StandardCharsets.UTF_8)
    }

}