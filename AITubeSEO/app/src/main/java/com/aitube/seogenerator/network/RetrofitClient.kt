package com.aitube.seogenerator.network

import com.aitube.seogenerator.utils.Constants
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private const val BASE_URL = "https://api.cerebras.ai/v1/"

    private val okHttpClient: OkHttpClient by lazy {

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()

            // 🔹 Add required headers for Cerebras API
            .addInterceptor { chain ->
                val original = chain.request()

                val request = original.newBuilder()
                    .header("Authorization", "Bearer ${Constants.CEREBRAS_API_KEY}")
                    .header("Content-Type", "application/json")
                    .method(original.method, original.body)
                    .build()

                chain.proceed(request)
            }

            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    val apiService: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
