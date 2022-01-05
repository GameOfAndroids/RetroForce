package com.tm78775.retroforce.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetroFactory {

    private fun getRetrofitClient(url: String): Retrofit {
        return Retrofit
            .Builder()
            .baseUrl(url)
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .client(getOkHttpClient())
            .build()
    }

    private fun getGson(): Gson {
        GsonBuilder().apply {
            setLenient()
        }.also {
            return it.create()
        }
    }

    private fun getOkHttpClient(): OkHttpClient {
        return OkHttpClient
            .Builder()
            .addInterceptor(getHttpLogger(null))
            .build()
    }

    private fun getHttpLogger(logLevel: HttpLoggingInterceptor.Level?): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = logLevel ?: HttpLoggingInterceptor.Level.BODY
        }
    }

    fun <T> createService(endPoint: String, serviceType: Class<T>): T {
        return getRetrofitClient(endPoint)
            .create(serviceType)
    }
}