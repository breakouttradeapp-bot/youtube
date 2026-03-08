package com.aitube.seogenerator.network

import com.aitube.seogenerator.models.CerebrasRequest
import com.aitube.seogenerator.models.CerebrasResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("v1/chat/completions")
    suspend fun generateContent(
        @Header("Authorization") authorization: String,
        @Body request: CerebrasRequest
    ): Response<CerebrasResponse>
}
