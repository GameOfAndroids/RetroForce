package com.tm78775.retroforce.service

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.tm78775.retroforce.model.AuthToken
import kotlinx.coroutines.flow.first

internal object TokenPersistenceService {

    private val Context.dataStore: DataStore<Preferences> by
    preferencesDataStore(name = "RetroStore")

    /**
     * If a user has been authenticated, then call this method to get the end user's [AuthToken].
     * @return The end user's [AuthToken].
     */
    suspend inline fun <reified T : AuthToken> getAuthToken(ctx: Context): T? {
        return readAuthToken(ctx)?.let {
            Gson().fromJson(it, T::class.java)
        }
    }

    /**
     * Call this to persist the user's [AuthToken].
     * @param token The [AuthToken] to save for future api calls.
     */
    suspend fun <T : AuthToken> saveAuthToken(ctx: Context, token: T?) {
        val gsonToken = Gson().toJson(token)
        val k = stringPreferencesKey("token")
        ctx.dataStore.edit {
            it[k] = gsonToken ?: ""
        }
    }

    /**
     * Helper method to read the [AuthToken] out of storage in its serialized form.
     * @return The auth token in its serialized form.
     */
    private suspend fun readAuthToken(ctx: Context): String? {
        val k = stringPreferencesKey("token")
        val prefs = ctx.dataStore.data.first()
        val stored = prefs[k]
        if(stored.isNullOrBlank())
            return null

        return stored
    }

}