package com.example.saleemacademy

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.example.saleemacademy.databinding.ActivityMainBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var adView: AdView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val tvForgotPass = findViewById<TextView>(R.id.tvForgotPass)
        val showPasswordIcon = findViewById<ImageView>(R.id.showPasswordIcon)

        MobileAds.initialize(this) {}

        adView = findViewById(R.id.adView1)

        var isPasswordVisible = false
        showPasswordIcon.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
               
                binding.etPass.transformationMethod = null
                showPasswordIcon.setImageResource(R.drawable.ic_hide_password_foreground)
            } else {

                binding.etPass.transformationMethod = PasswordTransformationMethod.getInstance()
                showPasswordIcon.setImageResource(R.drawable.ic_view_foreground)
            }
        }
        tvForgotPass.setOnClickListener {
            val email = binding.etEmail1.text.toString()
            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Password reset email sent successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                this,
                                "Failed to send password reset email. Please check your email address",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    this,
                    "Please enter your email address to reset the password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        tvSignUp.setOnClickListener() {
            Intent(this, SignUp::class.java).also {
                startActivity(it)
            }
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail1.text.toString()
            val pass = binding.etPass.text.toString()

            if(email.isNotEmpty() && pass.isNotEmpty()){
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val intent = Intent(this, Parentlogin::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Incorrect login details, Try again", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(
                    this,
                    "Please enter your email address to reset the password",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }


        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }

    override fun onPause() {
        adView.pause()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        adView.resume()
    }

    override fun onDestroy() {
        adView.destroy()
        super.onDestroy()
    }
}