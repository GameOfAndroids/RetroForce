package com.tm78775.retroforce

import android.app.Application
import com.tm78775.retroforce.model.AuthToken
import com.tm78775.retroforce.model.CommunityAuthToken
import com.tm78775.retroforce.service.TokenPersistenceService
import javax.inject.Inject

class CommunityAuthViewModel @Inject constructor(
    app: Application
) : AuthViewModel(app) {

    override suspend fun getAuthToken(): AuthToken? {
        return TokenPersistenceService.getAuthToken<CommunityAuthToken>(app)
    }

}