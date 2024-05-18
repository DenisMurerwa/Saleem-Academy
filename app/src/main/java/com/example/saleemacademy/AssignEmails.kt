package com.example.saleemacademy

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import java.util.concurrent.atomic.AtomicInteger

class AssignEmails : AppCompatActivity() {

    private lateinit var emailGrade1: EditText
    private lateinit var emailGrade2: EditText
    private lateinit var emailGrade3: EditText
    private lateinit var emailGrade4: EditText
    private lateinit var emailGrade5: EditText
    private lateinit var emailGrade6: EditText
    private lateinit var emailGrade7: EditText
    private lateinit var emailGrade8: EditText
    private lateinit var emailNursery: EditText


    private lateinit var saveButton: Button


    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.asignemails)

        // Initialize Firebase components
        auth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()

        // Initialize views
        emailGrade1 = findViewById(R.id.editEmailGrade1)
        emailGrade2 = findViewById(R.id.editEmailGrade2)
        emailGrade3 = findViewById(R.id.editEmailGrade3)
        emailGrade4 = findViewById(R.id.editEmailGrade4)
        emailGrade5 = findViewById(R.id.editEmailGrade5)
        emailGrade6 = findViewById(R.id.editEmailGrade6)
        emailGrade7 = findViewById(R.id.editEmailGrade7)
        emailGrade8 = findViewById(R.id.editEmailGrade8)
        emailNursery = findViewById(R.id.editEmailNursery)


        saveButton = findViewById(R.id.buttonSave)


        saveButton.setOnClickListener {
            saveEmails()
        }
    }

    private fun saveEmails() {
        // Validate email addresses and save to Firebase storage
        val emailNursery = emailNursery.text.toString().trim()
        val email1 = emailGrade1.text.toString().trim()
        val email2 = emailGrade2.text.toString().trim()
        val email3 = emailGrade3.text.toString().trim()
        val email4 = emailGrade4.text.toString().trim()
        val email5 = emailGrade5.text.toString().trim()
        val email6 = emailGrade6.text.toString().trim()
        val email7 = emailGrade7.text.toString().trim()
        val email8 = emailGrade8.text.toString().trim()

        // Validate the email addresses
        val validEmails = validateEmails(email1, email2, email3, email4, email5, email6, email7, email8)

        // If all emails are valid, save them to Firebase storage
        if (validEmails) {
            val emails = listOf(emailNursery, email1, email2, email3, email4, email5, email6, email7, email8)
            saveEmailsToFirebase(emails)
        } else {
            Toast.makeText(this, "Invalid email address(es)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateEmails(vararg emails: String): Boolean {
        // Validate each email address
        for (email in emails) {
            if (!isValidEmail(email)) {
                // Handle invalid email address
                return false
            }
        }
        return true
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun saveEmailsToFirebase(emails: List<String>) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val storageRef = storage.reference.child("emails/")
            val successCount = AtomicInteger(0)
            for ((index, email) in emails.withIndex()) {
                val gradeRef = storageRef.child("grade${index}")
                gradeRef.putBytes(email.toByteArray())
                    .addOnSuccessListener {
                        // Increment the success count
                        successCount.incrementAndGet()
                        // Check if all emails are saved successfully
                        if (successCount.get() == emails.size) {
                            // Display a single success message
                            Toast.makeText(this, "All emails saved successfully", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, "Failed to save email ${index + 1}: ${exception.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


}
