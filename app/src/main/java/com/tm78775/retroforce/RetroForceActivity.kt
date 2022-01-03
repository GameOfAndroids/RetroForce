package com.tm78775.retroforce

import android.content.Context
import android.content.Intent
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import com.google.gson.Gson
import com.tm78775.retroforce.login.LoginActivity
import com.tm78775.retroforce.model.*
import com.tm78775.retroforce.service.RetroFactory
import com.tm78775.retroforce.service.SessionRefreshService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.UnknownHostException

/**
 * Extend this class to enable RetroForce's authentication and session refreshing with Salesforce.
 * By extending this class, RetroForce will automatically show a login screen when
 * it has detected that no user has been previously logged in.
 * When RetroForce detects that the user's session has expired, it will automatically
 * attempt to start a new session with the refresh token. If no network connection is
 * available, then RetroForce will postpone the session refreshing. In the event that
 * the user's refresh token has been revoked, then RetroForce will prompt the user to
 * login again.
 */
abstract class RetroForceActivity : ComponentActivity() {

    /**
     * When this method is invoked, return the server to which you wish to connect.
     * @return The [Server] object configured for use in this build.
     */
    abstract fun getServer(): Server

    /**
     * When this method is invoked, return the [AuthTokenParser] necessary for your
     * needs. To use the default parser, please see the [SalesforceCommunityTokenParser].
     * @return The parser that will be used to extract [AuthToken] data from the returned
     * url at login time.
     */
    abstract fun getAuthTokenParser(): AuthTokenParser

    /**
     * This method will determine if a user is authenticated.
     * @return True if a user is authenticated. Otherwise false will be returned.
     */
    protected suspend fun userIsAuthenticated(): Boolean {
        val serializedToken = getAuthToken()
        return serializedToken != null
    }

    /**
     * Call this method to start the login activity. This will be launched on the main thread.
     */
    protected fun startLoginActivity() {
        if(Looper.myLooper() != Looper.getMainLooper()) {
            lifecycleScope.launch(Dispatchers.Main) {
                this@RetroForceActivity.startLoginActivity()
            }
        } else {
            Intent(this@RetroForceActivity, LoginActivity::class.java).apply {
                putExtra("server", getServer())
                putExtra("token_parser", getAuthTokenParser())
            }.also {
                startActivity(it)
                overridePendingTransition(R.anim.slide_up, R.anim.stay)
            }
        }
    }

    /**
     * Call this method to refresh the authenticated user's auth token.
     */
    @Throws
    protected suspend fun refreshToken() {
        getAuthToken()?.let { token ->
            getServer().also {
                val refreshService = RetroFactory.createService(
                    getRefreshServer(it.environment),
                    SessionRefreshService::class.java
                )

                try {
                    val resp = refreshService.refreshSession(
                        clientId = it.clientId,
                        secret = it.secret,
                        refreshToken = token.refreshToken
                    )

                    if(resp.code() == 200) {
                        resp.body()?.let { refreshResp ->
                            token.accessToken = refreshResp.accessToken
                            token.issuedAt = refreshResp.issuedAt
                            token.instanceUrl = refreshResp.instanceUrl
                            token.signature = refreshResp.signature
                            token.tokenType = refreshResp.tokenType
                            token.scope = refreshResp.scope.split(' ')
                            token.id = refreshResp.id
                        }
                    } else if(resp.code() >= 400) {
                        startLoginActivity()
                    }

                } catch (uhe: UnknownHostException) {
                    Log.d(this.toString(), "RetroForce is unable to reach the host " +
                            "at ${getRefreshServer(it.environment)}. Do you have a data connection?")
                    throw uhe
                }
            }
        }
    }

    /**
     * Helper method to get the token refresh endpoint.
     * @param environment The [ServerEnvironment] the user is working on.
     * @return The endpoint in String format.
     */
    private fun getRefreshServer(environment: ServerEnvironment): String {
        return when(environment) {
            ServerEnvironment.prod -> "https://login.salesforce.com/"
            ServerEnvironment.sandbox -> "https://test.salesforce.com/"
        }
    }

    /**
     * If a user has been authenticated, then call this method to get the end user's [AuthToken].
     * @return The end user's [AuthToken].
     */
    protected suspend fun getAuthToken(): AuthToken? {
        return readAuthToken()?.let {
            Gson().fromJson(it, AuthToken::class.java)
        }
    }

    /**
     * Call this to persist the user's [AuthToken].
     * @param token The [AuthToken] to save for future api calls.
     */
    protected suspend fun saveAuthToken(token: AuthToken?) {
        val gsonToken = Gson().toJson(token)
        val k = stringPreferencesKey("token")
        dataStore.edit {
            it[k] = gsonToken ?: ""
        }
    }

    /**
     * Helper method to read the [AuthToken] out of storage in its serialized form.
     * @return The auth token in its serialized form.
     */
    private suspend fun readAuthToken(): String? {
        val k = stringPreferencesKey("token")
        val prefs = dataStore.data.first()
        val stored = prefs[k]
        if(stored.isNullOrBlank())
            return null

        return stored
    }

    companion object {
        private val Context.dataStore: DataStore<Preferences> by
        preferencesDataStore(name = "RetroStore")
    }

}