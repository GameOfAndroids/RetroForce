package com.tm78775.retroforce.model

import java.io.Serializable

interface AuthToken : Serializable {
    val redirectUri: String
    var accessToken: String
    val refreshToken: String
    var signature: String
    var instanceUrl: String
    var idUrl: String
    var tokenType: String
    var issuedAt: String
    fun uid(): String
}