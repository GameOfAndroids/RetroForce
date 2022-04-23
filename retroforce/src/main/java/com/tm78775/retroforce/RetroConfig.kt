package com.tm78775.retroforce

import com.tm78775.retroforce.model.AuthToken
import com.tm78775.retroforce.model.ConnectedApp
import com.tm78775.retroforce.service.AuthTokenException
import com.tm78775.retroforce.service.ServerNotConfigured
import java.lang.RuntimeException

/**
 * At application startup, call [configureServer] to configure RetroForce and prepare it for
 * network operations.
 */
object RetroConfig {

    private var token: AuthToken? = null
    private var connectedApp: ConnectedApp? = null

    /**
     * Call this to get the id of the authenticated user.
     * @return The id of the authenticated user.
     */
    @Throws
    fun getUid(): String {
        token?.let {
            return@let it.uid()
        }

        throw RuntimeException("There is no authenticated user.")
    }

    /**
     * Call this to setup RetroForce's Server endpoint.
     * @param connectedApp The Server RetroForce should use.
     */
    @Suppress("WeakerAccess")
    fun configureServer(connectedApp: ConnectedApp) {
        this.connectedApp = connectedApp
    }

    /**
     * Call this to get the server the client is requesting to use.
     * @return The endpoint [ConnectedApp]
     */
    @Throws
    internal fun getServer(): ConnectedApp {
        if(connectedApp == null)
            throw ServerNotConfigured("The server has not been initialized. You must call " +
                    "RetroConfig.configureServer() before attempting network invocations.")
        return connectedApp!!
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