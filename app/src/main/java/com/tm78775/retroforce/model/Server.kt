package com.tm78775.retroforce.model

import java.io.Serializable

data class Server(
    val name: String,
    /** The [endpoint] should end with NO forward slash. */
    val endpoint: String,
    /** The [refreshTokenEndpoint] should end with a forward slash. */
    val refreshTokenEndpoint: String,
    val clientId: String,
    val secret: String,
    val redirectUri: String,
    val scope: List<String>
) : Serializable {
    init {
        if(endpoint.endsWith('/'))
            throw Exception("The endpoint must not end with a forward slash.")

        if(!refreshTokenEndpoint.endsWith('/'))
            throw Exception("The refreshTokenEndpoint must end with a forward slash.")
    }
}