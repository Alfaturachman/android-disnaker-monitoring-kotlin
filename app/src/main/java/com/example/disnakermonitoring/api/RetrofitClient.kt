package com.example.disnakermonitoring.api

import com.example.disnakermonitoring.api.ApiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val ip: String = "192.168.1.8"

    private val BASE_URL = "http://$ip/disnaker-monitoring/api/"

    public val BASE_URL_UPLOADS = "http://$ip/disnaker-monitoring/uploads/"

    private val client = OkHttpClient.Builder().build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }

    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}