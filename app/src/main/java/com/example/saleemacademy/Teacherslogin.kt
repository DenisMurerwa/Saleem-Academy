package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage


class Teacherslogin: AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_login)

        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        val btnNavigate: ImageView = findViewById(R.id.ivBack)

        btnNavigate.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Set onClickListeners for class buttons
        val buttons = arrayOf(
            findViewById<Button>(R.id.btn_nursery),
            findViewById<Button>(R.id.btn_grade1),
            findViewById<Button>(R.id.btn_grade2),
            findViewById<Button>(R.id.btn_grade3),
            findViewById<Button>(R.id.btn_grade4),
            findViewById<Button>(R.id.btn_grade5),
            findViewById<Button>(R.id.btn_grade6),
            findViewById<Button>(R.id.btn_grade7),
            findViewById<Button>(R.id.btn_grade8)
        )

        for (button in buttons) {
            button.setOnClickListener {
                // Get the class name from the button text
                val className = button.text.toString()

                // Get the currently logged-in user's email
                val currentUser = auth.currentUser
                val currentUserEmail = currentUser?.email

                // Fetch the assigned email for the class from Firebase Storage
                val classRef = storage.reference.child("emails/grade${getClassGrade(className)}")
                classRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                    val classEmail = String(bytes)

                    // Compare the class email with the logged-in user's email
                    if (currentUserEmail == classEmail) {
                        // Open the class
                        openClass(className)
                    } else {
                        // Show a message indicating access denied
                        Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show()
                    }
                }.addOnFailureListener { exception ->
                    Toast.makeText(
                        this,
                        "Failed to retrieve class email: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }


    private fun getClassGrade(className: String): Int {

        val parts = className.split(" ")

        return if (parts.size == 2 && parts[0] == "Grade") {

            parts[1].toInt()
        } else {
            0
        }
    }


    // Function to open the class
    private fun openClass(className: String) {
        when (className) {
            "Nursery" -> {
                val intent = Intent(this, Nursery::class.java)
                startActivity(intent)
            }

            "Grade 1" -> {
                val intent = Intent(this, Grade1::class.java)
                startActivity(intent)
            }

            "Grade 2" -> {
                val intent = Intent(this, Grade2::class.java)
                startActivity(intent)
            }
            "Grade 3" -> {
                val intent = Intent(this, Grade3::class.java)
                startActivity(intent)
            }
            "Grade 4" -> {
                val intent = Intent(this, Grade4::class.java)
                startActivity(intent)
            }
            "Grade 5" -> {
                val intent = Intent(this, Grade5::class.java)
                startActivity(intent)
            }
            "Grade 6" -> {
                val intent = Intent(this, Grade6::class.java)
                startActivity(intent)
            }
            "Grade 7" -> {
                val intent = Intent(this, Grade7::class.java)
                startActivity(intent)
            }
            "Grade 8" -> {
                val intent = Intent(this, Grade8::class.java)
                startActivity(intent)
            }

        }
    }
}