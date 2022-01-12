package com.tm78775.retroforce.model

class CommunityAuthToken(
    override val redirectUri: String,
    override var accessToken: String,
    override val refreshToken: String,
    val communityUrl: String,
    val communityId: String,
    override var signature: String,
    var scope: List<String>,
    override var instanceUrl: String,
    override var idUrl: String,
    override var tokenType: String,
    override var issuedAt: String
) : AuthToken {

    override fun uid(): String {
        return parseUid(idUrl)
    }

    private fun parseUid(idString: String): String {
        val i = idString.lastIndexOf('/') + 1
        return if(i > 0 && i < idString.length)
            idString.substring(i)
        else
            idString
    }
}