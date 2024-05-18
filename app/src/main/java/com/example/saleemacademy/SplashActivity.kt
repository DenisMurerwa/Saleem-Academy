package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)
        val image = findViewById<ImageView>(R.id.imageView)

        image.scaleX = 0f
        image.scaleY = 0f

        // Scale up the image with animation
        image.animate().setDuration(3500)
            .scaleX(1f)
            .scaleY(1f)
            .withEndAction {
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                finish()
            }
    }
}