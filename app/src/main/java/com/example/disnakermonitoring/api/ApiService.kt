package com.example.arsipsurat.api

import com.example.disnakermonitoring.model.Media
import com.example.disnakermonitoring.model.StatsTotalData
import com.example.disnakermonitoring.model.TambahMedia
import com.example.disnakermonitoring.model.TotalRekapData
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    // Login
    @POST("login.php")
    fun loginUser(@Body body: RequestBody): Call<ResponseBody>

    @POST("post_kompetitor.php")
    fun registerUser(@Body body: RequestBody): Call<ResponseBody>

    // Kontributor
    @Headers("Content-Type: application/json")
    @POST("get_rekap_kontributor.php")
    fun TotalDataKontributor(@Body requestBody: HashMap<String, Int>): Call<ApiResponse<TotalRekapData>>

    @Headers("Content-Type: application/json")
    @POST("get_stats_kontributor.php")
    fun StatsDataKontributor(@Body requestBody: HashMap<String, Int>): Call<ApiResponse<List<StatsTotalData>>>

    @Multipart
    @POST("post_media_kontributor.php")
    fun tambahMediaKontributor(
        @Part id_user: Int,
        @Part id_kategori: MultipartBody.Part,
        @Part nama: MultipartBody.Part,
        @Part judul: MultipartBody.Part,
        @Part url: MultipartBody.Part,
        @Part gambar: MultipartBody.Part,
        @Part deskripsi: MultipartBody.Part
    ): Call<ApiResponse<TambahMedia>>

    @Headers("Content-Type: application/json")
    @POST("get_media_kontributor.php")
    fun riwayatMediaKontributor(@Body request: Map<String, Int>): Call<ApiResponse<List<Media>>>

    // Pemimpin
    @Headers("Content-Type: application/json")
    @POST("get_rekap_pemimpin.php")
    fun TotalDataPemimpin(@Body requestBody: HashMap<String, Int>): Call<ApiResponse<TotalRekapData>>

    @Headers("Content-Type: application/json")
    @POST("get_stats_pemimpin.php")
    fun StatsDataPemimpin(@Body requestBody: HashMap<String, Int>): Call<ApiResponse<List<StatsTotalData>>>
}
