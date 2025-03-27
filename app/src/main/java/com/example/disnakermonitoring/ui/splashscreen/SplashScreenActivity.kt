package com.example.disnakermonitoring.ui.splashscreen

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.disnakermonitoring.MainActivity
import com.example.disnakermonitoring.R
import com.example.disnakermonitoring.ui.auth.LoginActivity

class SplashScreenActivity : AppCompatActivity() {

    private val splashScreenDelay: Long = 2000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash_screen)
        supportActionBar?.hide()

        // Handler
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, splashScreenDelay)
    }
}