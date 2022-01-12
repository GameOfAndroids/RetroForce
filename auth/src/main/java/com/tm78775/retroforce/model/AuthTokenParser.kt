package com.tm78775.retroforce.model

import java.io.Serializable

interface AuthTokenParser : Serializable {
    fun isRedirectUriDetected(server: Server, url: String): Boolean
    fun parseAuthToken(url: String): AuthToken
    fun parseRefreshToken(token: AuthToken, response: RefreshTokenResponse): AuthToken
}