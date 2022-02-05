package com.tm78775.retroforce

import com.tm78775.retroforce.model.Server
import com.tm78775.retroforce.service.ServerNotConfigured

object RetroConfig {

    private var server: Server? = null

    fun configureServer(server: Server) {
        this.server = server
    }

    @Throws
    fun getServer(): Server {
        if(server == null)
            throw ServerNotConfigured("The server has not been initialized. You must call " +
                    "RetroConfig.configureServer() before attempting network invocations.")
        return server!!
    }

}