package com.example.disnakermonitoring.ui.auth

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.disnakermonitoring.api.RetrofitClient
import com.example.disnakermonitoring.R
import com.google.android.material.textfield.TextInputEditText
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.function.LongFunction

class RegisterActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "RegisterActivity"
    }

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var etNama: TextInputEditText
    private lateinit var etTelp: TextInputEditText
    private lateinit var etAlamat: TextInputEditText
    private lateinit var buttonRegister: Button
    private lateinit var tvLogin: TextView

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        supportActionBar?.hide()

        window.statusBarColor = resources.getColor(R.color.white, theme)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        etEmail = findViewById(R.id.et_email)
        etPassword = findViewById(R.id.et_password)
        etNama = findViewById(R.id.et_nama)
        etTelp = findViewById(R.id.et_telp)
        etAlamat = findViewById(R.id.et_alamat)

        tvLogin = findViewById(R.id.tvLogin)
        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Tombol Register
        buttonRegister = findViewById(R.id.btn_register)
        buttonRegister.setOnClickListener {
            val nama = etNama.text.toString().trim()
            val telp = etTelp.text.toString().trim()
            val alamat = etAlamat.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Validasi input
            when {
                email.isEmpty() -> {
                    etEmail.error = "Email tidak boleh kosong"
                }
                password.isEmpty() -> {
                    etPassword.error = "Password tidak boleh kosong"
                }
                nama.isEmpty() -> {
                    etNama.error = "Nama Pemilik tidak boleh kosong"
                }
                telp.isEmpty() -> {
                    etTelp.error = "Nomor Telepon tidak boleh kosong"
                }
                alamat.isEmpty() -> {
                    etAlamat.error = "Alamat tidak boleh kosong"
                }
                else -> {
                    registerUser(nama, telp, alamat, email, password)
                }
            }
        }
    }

    private fun registerUser(nama: String, telp: String, alamat: String, email: String, password: String) {
        // Buat object JSON
        val requestData = mapOf(
            "email" to email,
            "password" to password,
            "nama" to nama,
            "no_hp" to telp,
            "alamat" to alamat
        )

        // Buat RequestBody dari Map
        val body = RequestBody.create(
            "application/json; charset=utf-8".toMediaType(),
            JSONObject(requestData).toString()
        )

        // Kirim ke server
        val call = RetrofitClient.instance.registerUser(body)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    try {
                        val result = response.body()?.string() ?: "{}"
                        Log.d(TAG, "Registration successful: $result")

                        val jsonResponse = JSONObject(result)
                        val status = jsonResponse.optBoolean("status", false)
                        val message = jsonResponse.optString("message", "Unknown error")

                        if (status && message.contains("Data pemasok berhasil ditambahkan!")) {
                            Toast.makeText(this@RegisterActivity, "Register berhasil", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@RegisterActivity, "Registration failed: $message", Toast.LENGTH_SHORT).show()
                            Log.e(TAG, "Registration failed: $message")
                        }
                    } catch (e: Exception) {
                        Toast.makeText(this@RegisterActivity, "Error parsing response: ${e.message}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "Error parsing response: ", e)
                    }
                } else {
                    Toast.makeText(this@RegisterActivity, "Server error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    Log.e(TAG, "Server response error: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(this@RegisterActivity, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Network error: ", t)
            }
        })
    }
}