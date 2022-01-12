package com.tm78775.retroforce

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.tm78775.retroforce.login.LoginActivity
import com.tm78775.retroforce.model.*
import com.tm78775.retroforce.model.AuthTokenParser
import com.tm78775.retroforce.service.SalesforceCommunityTokenParser
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

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

    private val viewModel: AuthViewModel by viewModels()
    private val loginContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        if (it.resultCode == RESULT_OK) {
            (it.data?.extras?.getSerializable("token") as? AuthToken)?.also {
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.onLoginSuccess(it)
                }
            }
        }
    }

    /**
     * Call this to get a [LiveData] object observing the [AuthToken].
     */
    fun getLiveAuthToken(): LiveData<AuthToken> = viewModel.liveAuthToken

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        // todo: test token to ensure it doesn't need refreshing.
        // todo: refresh token if it does need to be refreshed.
        // todo: if refreshing token results in unauthorized response, re-authenticate.
    }

    /**
     * When this method is invoked, return the [AuthTokenParser] necessary for your
     * needs. To use the default parser, please see the [SalesforceCommunityTokenParser].
     * @return The parser that will be used to extract [AuthToken] data from the returned
     * url at login time.
     */
    @Suppress("UNCHECKED_CAST")
    open fun getAuthTokenParser(): AuthTokenParser {
        return SalesforceCommunityTokenParser()
    }

    /**
     * When this method is invoked, return the server to which you wish to connect.
     * @return The [Server] object configured for use in this build.
     */
    abstract fun getServer(): Server

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            if (!userIsAuthenticated()) {
                startLoginActivity()
            }
        }
    }


    /**
     * This method will determine if a user is authenticated.
     * @return True if a user is authenticated. Otherwise false will be returned.
     */
    protected suspend fun userIsAuthenticated(): Boolean {
        return viewModel.userIsAuthenticated()
    }

    /**
     * This method will revoke the user's token remotely and delete the end-user's
     * auth token locally.
     */
    protected suspend fun logout() {
        viewModel.logout(getServer())
        startLoginActivity()
    }

    /**
     * Call this method to start the login activity. This will be launched on the main thread.
     */
    protected fun startLoginActivity() = lifecycleScope.launch(Dispatchers.Main) {
        loginContract.launch(
            Intent(
                this@RetroForceActivity,
                LoginActivity::class.java
            ).apply {
                putExtra("server", getServer())
                putExtra("token_parser", getAuthTokenParser())
            }
        )
        overridePendingTransition(R.anim.slide_up, R.anim.zoom_out)
    }

    /**
     * Call this method to refresh the authenticated user's auth token.
     */
    @Throws
    protected suspend fun refreshAuthToken() {
        try {
            viewModel.refreshSession(getServer(), getAuthTokenParser())
        } catch (ua: UnauthenticatedException) {
            // No token was found in the persisted store. Re-authenticate.
            startLoginActivity()
        } catch (re: RefreshException) {
            // The refresh token is likely no longer valid. Re-authenticate.
            startLoginActivity()
        } catch (uhe: UnknownHostException) {
            // Unable to reach host. Bad network connectivity or server unavailable.
        }
    }

}