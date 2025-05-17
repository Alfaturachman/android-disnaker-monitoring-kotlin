package com.example.disnakermonitoring.ui.kontributor

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.disnakermonitoring.api.ApiResponse
import com.example.disnakermonitoring.api.RetrofitClient
import com.example.disnakermonitoring.R
import com.example.disnakermonitoring.model.Media
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RiwayatMediaActivity : AppCompatActivity() {

    private var idUser: Int = -1
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RiwayatMediaAdapter

    // ActivityResultLauncher untuk menangkap hasil dari aktivitas lain
    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            refreshData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_riwayat_media_kontributor)
        supportActionBar?.hide()

        // Set status bar color dan mode light
        window.statusBarColor = resources.getColor(R.color.white, theme)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Button Kembali
        val btnKembali: ImageButton = findViewById(R.id.btnKembali)
        btnKembali.setOnClickListener {
            finish()
        }

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerViewRiwayatMedia)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Inisialisasi Adapter
        adapter = RiwayatMediaAdapter(emptyList(), startForResult)
        recyclerView.adapter = adapter

        // Ambil id_user dari SharedPreferences
        idUser = getUserIdFromSharedPreferences()
        if (idUser != -1) {
            fetchMediaKontributor(idUser)
        } else {
            Log.e("RiwayatMedia", "ID User tidak ditemukan di SharedPreferences")
        }
    }

    private fun fetchMediaKontributor(idUser: Int) {
        val requestBody = hashMapOf("id_user" to idUser)

        Log.d("RiwayatMedia", "Mengirim request ke server dengan body: $requestBody")

        val call = RetrofitClient.instance.riwayatMediaKontributor(requestBody)
        call.enqueue(object : Callback<ApiResponse<List<Media>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<Media>>>,
                response: Response<ApiResponse<List<Media>>>
            ) {
                Log.d("RiwayatMedia", "Response diterima dengan kode: ${response.code()}")

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody?.status == true) {
                        Log.d("RiwayatMedia", "Data berhasil diterima: ${responseBody.data}")
                        responseBody.data?.let {
                            adapter.updateData(it)
                        }
                    } else {
                        Log.e("RiwayatMedia", "Gagal mendapatkan data: ${responseBody?.message}")
                    }
                } else {
                    Log.e("RiwayatMedia", "Request gagal dengan kode: ${response.code()}, pesan: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<Media>>>, t: Throwable) {
                Log.e("RiwayatMedia", "Gagal menghubungi server: ${t.localizedMessage}", t)
            }
        })
    }

    private fun getUserIdFromSharedPreferences(): Int {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id_user", -1)
    }

    private fun refreshData() {
        fetchMediaKontributor(idUser)
    }
}