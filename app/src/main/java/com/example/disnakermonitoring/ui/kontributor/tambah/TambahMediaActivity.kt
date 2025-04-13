package com.example.disnakermonitoring.ui.kontributor.tambah

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.arsipsurat.api.ApiResponse
import com.example.arsipsurat.api.RetrofitClient
import com.example.disnakermonitoring.R
import com.example.disnakermonitoring.model.TambahMedia
import com.example.disnakermonitoring.ui.kontributor.RiwayatMediaActivity
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileInputStream

class TambahMediaActivity : AppCompatActivity() {

    private var idUser: Int = -1
    private var selectedKategoriId: Int = 0
    private lateinit var etNamaMedia: TextInputEditText
    private lateinit var etJudulMedia: TextInputEditText
    private lateinit var etUrlMedia: TextInputEditText
    private lateinit var spinnerKategoriMedia: Spinner
    private lateinit var etDeskripsiMedia: TextInputEditText
    private lateinit var tvNamaFileGambar: TextView
    private var selectedGambarUri: Uri? = null
    private lateinit var selectGambarLauncher: ActivityResultLauncher<Intent>
    private var selectedKategori: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_media_kontributor)
        supportActionBar?.hide()

        // Set status bar color dan mode light
        window.statusBarColor = resources.getColor(R.color.white, theme)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        // Inisialisasi komponen UI
        etNamaMedia = findViewById(R.id.etNamaMedia)
        etJudulMedia = findViewById(R.id.etJudulMedia)
        etUrlMedia = findViewById(R.id.etUrlMedia)
        spinnerKategoriMedia = findViewById(R.id.spinnerKategoriMedia)
        etDeskripsiMedia = findViewById(R.id.etDeskripsiMedia)
        tvNamaFileGambar = findViewById(R.id.tvNamaFileGambar)

        idUser = getUserIdFromSharedPreferences()

        // Button Kembali
        val btnKembali: ImageButton = findViewById(R.id.btnKembali)
        btnKembali.setOnClickListener {
            finish()
        }

        // Button Simpan
        val btnSimpan: Button = findViewById(R.id.buttonLogin)
        btnSimpan.setOnClickListener {
            simpanData()
        }

        // Inisialisasi Spinner Kategori Media
        val kategoriMediaList = listOf(
            "Pilih Kategori" to 0,
            "Lowongan" to 1,
            "Pelatihan" to 2,
            "Bisnis" to 3,
            "UMKM" to 4,
            "Pabrik" to 5,
            "Buruh" to 6,
            "PHK" to 7,
            "Mediasi" to 8
        )

        // Ambil hanya nama kategori untuk ditampilkan di Spinner
        val kategoriNamaList = kategoriMediaList.map { it.first }

        val kategoriAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, kategoriNamaList)
        kategoriAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerKategoriMedia.adapter = kategoriAdapter

        spinnerKategoriMedia.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedKategori = kategoriNamaList[position] // Simpan nama kategori
                selectedKategoriId = kategoriMediaList[position].second // Simpan ID kategori
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Tidak ada yang dipilih
            }
        }

        // Di dalam onCreate
        selectGambarLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                uri?.let {
                    selectedGambarUri = it
                    val fileName = getFileName(it)
                    tvNamaFileGambar.text = fileName

                    Log.d("GAMBAR_UPLOAD", "File URI: $uri")
                    Log.d("GAMBAR_UPLOAD", "File Name: $fileName")
                }
            } else {
                Log.d("GAMBAR_UPLOAD", "File selection canceled")
            }
        }
    }

    private fun simpanData() {
        val namaMedia = etNamaMedia.text.toString()
        val judulMedia = etJudulMedia.text.toString()
        val urlMedia = etUrlMedia.text.toString()
        val deskripsiMedia = etDeskripsiMedia.text.toString()

        // Validasi Spinner Kategori
        if (selectedKategoriId == 0) { // ID 0 berarti "Pilih Kategori"
            Toast.makeText(this, "Harap pilih kategori media", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi URL Media
        if (urlMedia.isEmpty()) {
            Toast.makeText(this, "URL Media tidak boleh kosong", Toast.LENGTH_SHORT).show()
            return
        }

        // Validasi File PDF
        if (selectedGambarUri == null) {
            Toast.makeText(this, "Pilih file PDF", Toast.LENGTH_SHORT).show()
            return
        }

        val filePart = getFilePart(selectedGambarUri!!) ?: return

        // Contoh konversi data ke MultipartBody.Part
        val idKategoriPart = MultipartBody.Part.createFormData("id_kategori", selectedKategoriId.toString())
        val namaPart = MultipartBody.Part.createFormData("nama", namaMedia)
        val judulPart = MultipartBody.Part.createFormData("judul", judulMedia)
        val urlPart = MultipartBody.Part.createFormData("url", urlMedia)
        val deskripsiPart = MultipartBody.Part.createFormData("deskripsi", deskripsiMedia)

        RetrofitClient.instance.tambahMediaKontributor(
            idUser,
            idKategoriPart,
            namaPart,
            judulPart,
            urlPart,
            filePart,
            deskripsiPart
        ).enqueue(object : Callback<ApiResponse<TambahMedia>> {
            override fun onResponse(
                call: Call<ApiResponse<TambahMedia>>,
                response: Response<ApiResponse<TambahMedia>>
            ) {
                if (response.isSuccessful) {
                    val result = response.body()
                    if (result != null && result.status) {
                        // Log data yang diinput
                        Log.d("TambahMediaActivity", "Nama Media: $namaMedia")
                        Log.d("TambahMediaActivity", "Judul Media: $judulMedia")
                        Log.d("TambahMediaActivity", "URL Media: $urlMedia")
                        Log.d("TambahMediaActivity", "Kategori Media: $selectedKategori (ID: $selectedKategoriId)")
                        Log.d("TambahMediaActivity", "Deskripsi Media: $deskripsiMedia")
                        Log.d("TambahMediaActivity", "PDF URI: $selectedGambarUri")

                        Toast.makeText(this@TambahMediaActivity, "Media berhasil disimpan!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@TambahMediaActivity, RiwayatMediaActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Log.e("TambahMedia", "Gagal menyimpan media. Error: $errorBody")
                        Toast.makeText(this@TambahMediaActivity, "Gagal menyimpan media!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("TambahMedia", "Response error: ${response.code()} - ${response.message()}. Error body: $errorBody")
                    Toast.makeText(this@TambahMediaActivity, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<TambahMedia>>, t: Throwable) {
                Log.e("TambahMedia", "Gagal menghubungi server: ${t.message}", t)
                Toast.makeText(this@TambahMediaActivity, "Gagal menghubungi server!", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun selectGambar(view: View) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*" // Menerima semua jenis gambar
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        selectGambarLauncher.launch(intent)
    }

    private fun getFileName(uri: Uri): String {
        var result = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    result = cursor.getString(index)
                }
            }
        }
        return result
    }

    private fun getFilePart(uri: Uri): MultipartBody.Part? {
        val fileDescriptor = contentResolver.openFileDescriptor(uri, "r") ?: return null
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val file = File(cacheDir, getFileName(uri))
        file.outputStream().use { output -> inputStream.copyTo(output) }

        // Tentukan tipe MIME berdasarkan ekstensi file
        val mimeType = contentResolver.getType(uri) ?: "image/*"
        val requestFile = file.asRequestBody(mimeType.toMediaTypeOrNull())
        return MultipartBody.Part.createFormData("gambar", file.name, requestFile)
    }

    private fun getUserIdFromSharedPreferences(): Int {
        val sharedPreferences = getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        return sharedPreferences.getInt("id_user", -1)
    }
}