package com.tm78775.retroforce

import com.tm78775.retroforce.model.AuthToken
import com.tm78775.retroforce.model.Server
import com.tm78775.retroforce.service.AuthTokenException
import com.tm78775.retroforce.service.ServerNotConfigured

object RetroConfig {

    private var token: AuthToken? = null
    private var server: Server? = null

    /**
     * Call this to setup RetroForce's Server endpoint.
     * @param server The Server RetroForce should use.
     */
    fun configureServer(server: Server) {
        this.server = server
    }

    /**
     * Call this to get the server the client is requesting to use.
     * @return The endpoint [Server]
     */
    @Throws
    internal fun getServer(): Server {
        if(server == null)
            throw ServerNotConfigured("The server has not been initialized. You must call " +
                    "RetroConfig.configureServer() before attempting network invocations.")
        return server!!
    }

    /**
     * Call this when the token is being restored or has been refreshed.
     * @param token The refreshed [AuthToken].
     */
    internal fun updateToken(token: AuthToken) {
        this.token = token
    }

    /**
     * Call this when the token is no longer valid.
     */
    internal fun onTokenRevoked() {
        token = null
    }

    /**
     * Call this to get the most up-to-date session token.
     * @return The user's [AuthToken].
     */
    internal fun getToken(): AuthToken {
        if(token == null)
            throw AuthTokenException("The AuthToken is null. Cannot execute network methods" +
                    "without an authenticated user.")

        return token!!
    }

}