package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Teacherslogin : AppCompatActivity() {

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
                val className = button.text.toString()
                validateUserEmailForClass(className)
            }
        }
    }

    private fun validateUserEmailForClass(className: String) {
        val currentUser = auth.currentUser
        val currentUserEmail = currentUser?.email ?: return Toast.makeText(this, "User email is null", Toast.LENGTH_SHORT).show()

        val grade = getClassGrade(className)
        val classRef = storage.reference.child("emails/grade$grade")

        classRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val classEmail = String(bytes, Charsets.UTF_8)
            if (currentUserEmail == classEmail) {
                openClass(className)
            } else {
                Toast.makeText(this, "Access denied", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Failed to retrieve class email: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getClassGrade(className: String): Int {
        val parts = className.split(" ")
        return if (parts.size == 2 && parts[0] == "Grade") parts[1].toInt() else 0
    }

    private fun openClass(className: String) {
        val intent = when (className) {
            "Nursery" -> Intent(this, Nursery::class.java)
            "Grade 1" -> Intent(this, Grade1::class.java)
            "Grade 2" -> Intent(this, Grade2::class.java)
            "Grade 3" -> Intent(this, Grade3::class.java)
            "Grade 4" -> Intent(this, Grade4::class.java)
            "Grade 5" -> Intent(this, Grade5::class.java)
            "Grade 6" -> Intent(this, Grade6::class.java)
            "Grade 7" -> Intent(this, Grade7::class.java)
            "Grade 8" -> Intent(this, Grade8::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }
}
