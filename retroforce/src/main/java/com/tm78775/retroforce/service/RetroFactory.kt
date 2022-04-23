package com.tm78775.retroforce.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.Level

@PublishedApi
internal object RetroFactory {

    private fun getRetrofitClient(authToken: String?, url: String): Retrofit {
        return Retrofit.Builder()
            .baseUrl("$url/")
            .addCallAdapterFactory(CoroutineCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create(getGson()))
            .client(getOkHttpClient(authToken))
            .build()
    }

    private fun getGson(): Gson {
        return GsonBuilder().apply {
            setLenient()
        }.create()
    }

    private fun getOkHttpClient(authToken: String?): OkHttpClient {
        val builder = OkHttpClient
            .Builder()
            .addInterceptor(getHttpLogger(null))

        if(authToken != null)
            builder.addInterceptor(getAuthInterceptor(authToken))

        return builder.build()
    }

    private fun getHttpLogger(logLevel: HttpLoggingInterceptor.Level?): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = logLevel ?: HttpLoggingInterceptor.Level.BODY
        }
    }

    private fun getAuthInterceptor(authToken: String): Interceptor {
        return object : Interceptor {
            override fun intercept(chain: Interceptor.Chain): Response {
                val req = chain.request()
                    .newBuilder()
                    .addHeader(
                        "Authorization",
                        buildAuthToken(authToken)
                    ).build()
                return chain.proceed(req)
            }
        }
    }

    private fun buildAuthToken(authToken: String): String {
        return "Bearer $authToken"
    }

    fun <T> createAuthenticatedService(
        authToken: String,
        endPoint: String,
        serviceType: Class<T>
    ): T {
        return getRetrofitClient(authToken, endPoint)
            .create(serviceType)
    }

    fun <T> createService(endPoint: String, serviceType: Class<T>): T {
        return getRetrofitClient(null, endPoint)
            .create(serviceType)
    }

}