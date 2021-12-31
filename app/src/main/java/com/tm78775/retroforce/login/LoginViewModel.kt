package com.tm78775.retroforce.login

import android.util.Log
import androidx.lifecycle.ViewModel
import com.tm78775.retroforce.model.Server
import java.lang.StringBuilder

internal class LoginViewModel : ViewModel() {

    private val authPath = "/services/oauth2/authorize"

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
}