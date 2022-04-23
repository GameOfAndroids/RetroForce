package com.tm78775.retroforce.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.tm78775.retroforce.RetroConfig
import com.tm78775.retroforce.RetroForce
import com.tm78775.retroforce.model.*
import com.tm78775.retroforce.service.*
import com.tm78775.retroforce.service.RetroFactory
import com.tm78775.retroforce.service.SessionService
import com.tm78775.retroforce.service.TokenPersistenceService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.StringBuilder
import java.net.UnknownHostException
import javax.inject.Inject

abstract class AuthViewModel @Inject constructor(
    protected val app: Application
) : AndroidViewModel(app) {

    protected abstract suspend fun getAuthToken(): AuthToken?
    private val _liveAuthToken = MutableLiveData<AuthToken>()
    val liveAuthToken: LiveData<AuthToken> = _liveAuthToken

    init {
        viewModelScope.launch(Dispatchers.IO) {
            getAuthToken()?.also {
                RetroConfig.updateToken(it)
                _liveAuthToken.postValue(it)
            }
        }
    }

    /**
     * Call this method to generate the auth endpoint that the web view should use
     * to authenticate the user with salesforce.
     * @param connectedApp The [ConnectedApp] we're using.
     * @param deviceId The id of the user's device.
     * @return A url ready to be fed to a web view.
     */
    fun generateAuthEndpoint(connectedApp: ConnectedApp, deviceId: String): String {
        return StringBuilder().apply {
            append(connectedApp.endpoint)
            append(authPath)
            append("?display=touch")
            append("&response_type=token")
            append("&client_id=${connectedApp.clientId}")

            if(connectedApp.scope.isNotEmpty()) {
                append("&scope=")
                for (i in connectedApp.scope.indices) {
                    append(connectedApp.scope[i])

                    try {
                        if (i < connectedApp.scope.size - 1)
                            append("%20")
                    } catch (e: Exception) {
                        Log.e(this.toString(), "Scope parse error.", e)
                    }
                }
            }
            append("&redirect_uri=${connectedApp.redirectUri}")
            append("&device_id=$deviceId")
        }.toString()
    }

    /**
     * Call this method to refresh the end user's session.
     * @param connectedApp The [ConnectedApp] environment.
     * @throws UnknownHostException if the endpoint cannot be reached due to network
     * connectivity or server is unavailable.
     * @throws RefreshException in the event that a refresh attempt did not return
     * a code of 200.
     * @throws UnauthenticatedException if the user has not authenticated before invoking
     * [refreshSession].
     */
    @Suppress("UNCHECKED_CAST")
    @Throws
    suspend fun refreshSession(connectedApp: ConnectedApp, tokenParser: AuthTokenParser) {
        val token = getAuthToken()
            ?: throw UnauthenticatedException("No auth token was stored. No user is authenticated.")

        val authService = RetroFactory.createService(
            getRefreshServer(connectedApp.environment),
            SessionService::class.java
        )

        try {
            val resp = authService.refreshSession(
                clientId = connectedApp.clientId,
                secret = connectedApp.secret,
                refreshToken = token.refreshToken
            )

            if(resp.code() == 200)
                resp.body()?.let { refreshResp ->
                    val refreshedToken = tokenParser.parseRefreshToken(token, refreshResp)
                    TokenPersistenceService.saveAuthToken(app, refreshedToken)
                    RetroConfig.updateToken(refreshedToken)
                }
            else if(resp.code() >= 400){
                TokenPersistenceService.saveAuthToken(app, null)
                _liveAuthToken.postValue(null)
                RetroConfig.onTokenRevoked()
                throw RefreshException(
                    "Refreshing the AuthToken resulted in a response " +
                            "of ${resp.code()} and an errorBody of ${resp.errorBody()}."
                )
            }
        } catch (uhe: UnknownHostException) {
            Log.d(this.toString(), "RetroForce is unable to reach the host " +
                    "at ${getRefreshServer(connectedApp.environment)}. Do you have a data connection?")
            throw uhe
        }
    }

    /**
     * Call this to determine if a user has logged in and has not logged out.
     * @return True if the library has an authenticated user. Otherwise false.
     */
    suspend fun userIsAuthenticated(): Boolean {
        return getAuthToken() != null
    }

    /**
     * Call this method when the user successfully authenticates. This will persist
     * the auth token.
     * @param token
     */
    suspend fun onLoginSuccess(token: AuthToken) {
        TokenPersistenceService.saveAuthToken(app, token)
        RetroConfig.updateToken(token)
        _liveAuthToken.postValue(token)
    }

    /**
     * Call this method to update the user's profile.
     */
    @Throws
    suspend fun refreshUserProfile(): AuthUser? {
        val token = RetroConfig.getToken()
        val query = "SELECT Id, FirstName, LastName, Email FROM User WHERE Id = '${token.uid()}'"
        val json: JsonArray? = RetroForce.query(query)
        val u = Gson().fromJson(json?.first(), AuthUser::class.java)
        Log.d(this::class.java.simpleName, "User: ${u.firstName} ${u.lastName}")
        return u
    }

    /**
     * Call this method to log the user out.
     * @param connectedApp The server instance the app is using.
     */
    suspend fun logout(connectedApp: ConnectedApp) {
        val token = getAuthToken() ?: return
        val authService = RetroFactory.createService(
            getRefreshServer(connectedApp.environment),
            SessionService::class.java
        )

        authService.logout(token.accessToken)
        TokenPersistenceService.saveAuthToken(app, null)
        _liveAuthToken.postValue(null)
        RetroConfig.onTokenRevoked()
    }

    /**
     * Helper method to get the token refresh endpoint.
     * @param environment The [AppEnvironment] the user is working on.
     * @return The endpoint in String format.
     */
    private fun getRefreshServer(environment: AppEnvironment): String {
        return when(environment) {
            AppEnvironment.Prod -> prodAuthEndpoint
            AppEnvironment.Sandbox -> sandboxAuthEndpoint
        }
    }

    companion object {
        private const val authPath = "/services/oauth2/authorize"
        private const val prodAuthEndpoint = "https://login.salesforce.com/"
        private const val sandboxAuthEndpoint = "https://test.salesforce.com/"
    }

}