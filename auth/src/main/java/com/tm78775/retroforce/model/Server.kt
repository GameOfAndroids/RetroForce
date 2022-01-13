package com.tm78775.retroforce.model

import java.io.Serializable

data class Server(
    val environment: ServerEnvironment,
    val name: String,
    /** The [endpoint] must not end with a forward slash. */
    val endpoint: String,
    val clientId: String,
    val secret: String,
    val redirectUri: String,
    val scope: List<String>
) : Serializable {
    init {
        if(endpoint.endsWith('/'))
            throw Exception("The endpoint must not end with a forward slash.")
    }
}

enum class ServerEnvironment {
    Prod, Sandbox
}