package com.tm78775.retroforce.model

class AuthToken(
    val redirectUri: String,
    var accessToken: String,
    val refreshToken: String,
    val communityUrl: String,
    val communityId: String,
    var signature: String,
    var scope: List<String>,
    var instanceUrl: String,
    id: String,
    var tokenType: String,
    var issuedAt: String
) {
    var id: String = id
    set(value) {
        val index = value.lastIndexOf('/', 0)
        if (index > -1)
            field = value.substring(index)
    }
}
