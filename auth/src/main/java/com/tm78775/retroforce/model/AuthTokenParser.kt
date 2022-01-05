package com.tm78775.retroforce.model

import java.io.Serializable

interface AuthTokenParser : Serializable {
    fun isRedirectUriDetected(server: Server, url: String): Boolean
    fun <T> parseAuthToken(url: String): T
}