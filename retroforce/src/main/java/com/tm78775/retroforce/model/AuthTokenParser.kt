package com.tm78775.retroforce.model

import com.tm78775.retroforce.service.RefreshTokenResponse
import java.io.Serializable

interface AuthTokenParser : Serializable {
    fun isRedirectUriDetected(connectedApp: ConnectedApp, url: String): Boolean
    fun parseAuthToken(url: String): AuthToken
    fun parseRefreshToken(token: AuthToken, response: RefreshTokenResponse): AuthToken
}