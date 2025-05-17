package com.example.disnakermonitoring.ui.kontributor.detail

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.disnakermonitoring.api.RetrofitClient
import com.example.disnakermonitoring.R

class DetailMediaActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_media_kontributor)
        supportActionBar?.hide()

        // Set status bar color dan mode light
        window.statusBarColor = resources.getColor(R.color.white, theme)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Ambil data dari intent
        val judul = intent.getStringExtra("judul")
        val url = intent.getStringExtra("url")
        val status = intent.getStringExtra("status")
        val tanggal = intent.getStringExtra("tanggal")
        val deskripsi = intent.getStringExtra("deskripsi")
        val view = intent.getIntExtra("view", 0)
        val gambar = intent.getStringExtra("gambar")

        // Inisialisasi view
        val tvJudulMedia = findViewById<TextView>(R.id.tvJudulMedia)
        val tvUrlMedia = findViewById<TextView>(R.id.tvUrlMedia)
        val tvStatus = findViewById<TextView>(R.id.tvStatus)
        val tvTanggal = findViewById<TextView>(R.id.tvTanggal)
        val tvDeskripsiKasus = findViewById<TextView>(R.id.tvDeskripsiKasus)
        val tvView = findViewById<TextView>(R.id.tvView)
        val imageView = findViewById<ImageView>(R.id.imageView)

        // Set data ke view
        tvJudulMedia.text = judul ?: "-"
        tvUrlMedia.text = url ?: "-"
        tvStatus.text = status ?: "-"
        tvTanggal.text = tanggal ?: "-"
        tvDeskripsiKasus.text = deskripsi ?: "-"
        tvView.text = "$view tayangan"

        // Load gambar (gunakan Glide atau Picasso)
        Glide.with(this)
            .load("${RetrofitClient.BASE_URL_UPLOADS}${gambar}")
            .placeholder(R.drawable.ic_launcher_background)
            .error(R.drawable.ic_launcher_background)
            .into(imageView)
    }
}