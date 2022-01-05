package com.tm78775.retroforce.model

import java.io.Serializable

class AuthToken(
    val redirectUri: String,
    var accessToken: String,
    val refreshToken: String,
    val communityUrl: String,
    val communityId: String,
    var signature: String,
    var scope: List<String>,
    var instanceUrl: String,
    idUrl: String,
    var tokenType: String,
    var issuedAt: String
) : Serializable {

    var uid: String = parseUid(idUrl)
        private set

    var idUrl: String = idUrl
        set(value) {
            field = value
            uid = parseUid(field)
        }

    private fun parseUid(idString: String): String {
        val i = idString.lastIndexOf('/') + 1
        return if(i > 0 && i < idString.length)
            idString.substring(i)
        else
            idString
    }
}
