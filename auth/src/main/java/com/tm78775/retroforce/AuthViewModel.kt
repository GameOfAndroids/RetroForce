package com.tm78775.retroforce

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.tm78775.retroforce.model.*
import com.tm78775.retroforce.service.RetroFactory
import com.tm78775.retroforce.service.SessionService
import com.tm78775.retroforce.service.TokenPersistenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.net.UnknownHostException
import javax.inject.Inject

class AuthViewModel @Inject constructor(
    private val app: Application,
) : AndroidViewModel(app) {

    private val authPath = "/services/oauth2/authorize"
    private val _authToken = MutableLiveData<AuthToken>()
    val liveAuthToken: LiveData<AuthToken> = _authToken

    init {
        viewModelScope.launch(Dispatchers.IO) {
            TokenPersistenceService.getAuthToken(app).also {
                _authToken.postValue(it)
            }
        }
    }

    /**
     * Call this method to generate the auth endpoint that the web view should use
     * to authenticate the user with salesforce.
     * @param server The [Server] we're using.
     * @param deviceId The id of the user's device.
     * @return A url ready to be fed to a web view.
     */
    fun generateAuthEndpoint(server: Server, deviceId: String): String {
        return StringBuilder().apply {
            append(server.endpoint)
            append(authPath)
            append("?display=touch")
            append("&response_type=token")
            append("&client_id=${server.clientId}")

            if(server.scope.isNotEmpty()) {
                append("&scope=")
                for (i in server.scope.indices) {
                    append(server.scope[i])

                    try {
                        if (i < server.scope.size - 1)
                            append("%20")
                    } catch (e: Exception) {
                        Log.e(this.toString(), "Scope parse error.", e)
                    }
                }
            }
            append("&redirect_uri=${server.redirectUri}")
            append("&device_id=$deviceId")
        }.toString()
    }

    /**
     * Call this method to refresh the end user's session.
     * @param server The [Server] environment.
     * @throws UnknownHostException if the endpoint cannot be reached due to network
     * connectivity or server is unavailable.
     * @throws RefreshException in the event that a refresh attempt did not return
     * a code of 200.
     */
    @Suppress("UNCHECKED_CAST")
    @Throws
    suspend fun refreshSession(server: Server, tokenParser: AuthTokenParser) {
        val token = TokenPersistenceService.getAuthToken(app)
            ?: throw UnauthenticatedException("No auth token was stored. No user is authenticated.")

        val authService = RetroFactory.createService(
            getRefreshServer(server.environment),
            SessionService::class.java
        )

        try {
            val resp = authService.refreshSession(
                clientId = server.clientId,
                secret = server.secret,
                refreshToken = token.refreshToken
            )

            if(resp.code() == 200)
                resp.body()?.let { refreshResp ->
                    val refreshedToken = tokenParser.parseRefreshToken(token, refreshResp)
                    TokenPersistenceService.saveAuthToken(app, refreshedToken)
                    _authToken.postValue(token)
                }

            _authToken.postValue(null)
            throw RefreshException("Refreshing the AuthToken resulted in a response " +
                    "of ${resp.code()} and an errorBody of ${resp.errorBody()}.")

        } catch (uhe: UnknownHostException) {
            Log.d(this.toString(), "RetroForce is unable to reach the host " +
                    "at ${getRefreshServer(server.environment)}. Do you have a data connection?")
            throw uhe
        }
    }

    /**
     * Call this to determine if a user has logged in and has not logged out.
     * @return True if the library has an authenticated user. Otherwise false.
     */
    suspend fun userIsAuthenticated(): Boolean {
        return TokenPersistenceService.getAuthToken(app) != null
    }

    /**
     * Call this method when the user successfully authenticates. This will persist
     * the auth token.
     * @param token
     */
    suspend fun onLoginSuccess(token: AuthToken) {
        TokenPersistenceService.saveAuthToken(app, token)
        _authToken.postValue(token)
    }

    /**
     * Call this method to log the user out.
     * @param server The server instance the app is using.
     */
    suspend fun logout(server: Server) {
        val token = TokenPersistenceService.getAuthToken(app) ?: return
        val authService = RetroFactory.createService(
            getRefreshServer(server.environment),
            SessionService::class.java
        )

        authService.logout(token.accessToken)
        TokenPersistenceService.saveAuthToken(app, null)
        _authToken.postValue(null)
    }

    /**
     * Helper method to get the token refresh endpoint.
     * @param environment The [ServerEnvironment] the user is working on.
     * @return The endpoint in String format.
     */
    private fun getRefreshServer(environment: ServerEnvironment): String {
        return when(environment) {
            ServerEnvironment.Prod -> "https://login.salesforce.com/"
            ServerEnvironment.Sandbox -> "https://test.salesforce.com/"
        }
    }

}