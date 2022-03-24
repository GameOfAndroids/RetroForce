package com.tm78775.retroforce

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.tm78775.retroforce.login.LoginActivity
import com.tm78775.retroforce.model.*
import com.tm78775.retroforce.model.AuthTokenParser
import com.tm78775.retroforce.service.RefreshException
import com.tm78775.retroforce.service.SalesforceCommunityTokenParser
import com.tm78775.retroforce.service.SessionExpiredException
import com.tm78775.retroforce.service.UnauthenticatedException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    private val viewModel: CommunityAuthViewModel by viewModels()
    private var suppressLogin: Boolean = false

    /**
     * Helper method to get a [LiveData] object to observe the available auth token when
     * it is available or has changed.
     * @return A LiveData object observing the [AuthToken].
     */
    protected fun getLiveAuthToken(): LiveData<AuthToken> {
        return viewModel.liveAuthToken
    }

    // Contract to receive data when launching activity for result.
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
     * If this is set to true, the [RetroForceActivity] will not navigate automatically
     * to the [LoginActivity] if there is no logged in user. This is useful if the child
     * [ComponentActivity] requires the authentication status, but wants to reserve
     * Authentication for a later time or in a different UI context. Invoke this in your
     * [onCreate] method before calling super.[onCreate].
     * @param suppress True to suppress automatic login. Defaulted to false.
     */
    fun suppressLogin(suppress: Boolean) {
        suppressLogin = suppress
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch(Dispatchers.IO) {
            delay(1000)
            if (!userIsAuthenticated() && !suppressLogin) {
                Log.d(this::class.simpleName, "There is no authenticated user. Navigating to ${LoginActivity::class.simpleName}")
                startLoginActivity()
            } else {
                performSessionValidation()
            }
        }
    }

    /**
     * This method will ensure that the end-user's session is still valid. If the session is
     * invalid, the [RetroForceActivity] will try to refresh the session. If it fails to refresh
     * the session due to an unauthorized exception, then the user will be navigated to the [LoginActivity].
     */
    private fun performSessionValidation() {
        lifecycleScope.launch(Dispatchers.IO) {
            Log.d(this::class.simpleName, "Ensuring session is not expired...")
            try {
                viewModel.refreshUserProfile()
                Log.d(this::class.simpleName, "Session is valid. No authentication action required.")
            } catch (see: SessionExpiredException) {
                Log.d(this::class.simpleName, "Session has expired.")
                refreshAuthToken()
            } catch (uhe: UnknownHostException) {
                Log.e(this::class.simpleName, "Unknown Host Exception: Bad network connectivity " +
                        "or server unavailable. Re-authentication and session refresh postponed.")
            } catch (e: Exception) {
                // TODO: CRASHLYTICS
                Log.e(this::class.simpleName, "THIS EXCEPTION IS UNACCOUNTED FOR AND NEEDS TO BE ADDRESSED! SEE ERROR: ${e.localizedMessage}", e)
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
        viewModel.logout(RetroConfig.getServer())
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
                putExtra("server", RetroConfig.getServer())
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
        Log.d(this::class.simpleName, "Refreshing auth token...")
        try {
            viewModel.refreshSession(RetroConfig.getServer(), getAuthTokenParser())
        } catch (ua: UnauthenticatedException) {
            // No token was found in the persisted store. Re-authenticate.
            startLoginActivity()
        } catch (re: RefreshException) {
            // The refresh token is likely no longer valid. Re-authenticate.
            startLoginActivity()
        } catch (uhe: UnknownHostException) {
            // Unable to reach host. Bad network connectivity or server unavailable.
            Log.d(this::class.simpleName, "Unable to connect to host to refresh auth token. " +
                    "Re-authentication is not required at this time.")
        }
    }

}