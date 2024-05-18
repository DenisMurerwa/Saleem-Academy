package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class Admin: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        val layoutAssignEmails = findViewById<LinearLayout>(R.id.layoutAssignEmails)
        val layoutUploadPastPapers = findViewById<LinearLayout>(R.id.layoutUploadPastPapers)
        val layoutUpdateEvents = findViewById<LinearLayout>(R.id.layoutUpdateEvents)
        val layoutUpdateNotes = findViewById<LinearLayout>(R.id.layoutUpdateNotes)
        val layoutAddContacts = findViewById<LinearLayout>(R.id.layoutAddContacts)

        // Set click listeners
        layoutAssignEmails.setOnClickListener {
            startActivity(Intent(this, AssignEmails::class.java))
        }

        layoutUploadPastPapers.setOnClickListener {
            startActivity(Intent(this, UploadPastPapers::class.java))
        }

        layoutUpdateEvents.setOnClickListener {
            startActivity(Intent(this, UpdateEvents::class.java))
        }

        layoutUpdateNotes.setOnClickListener {
            startActivity(Intent(this, UpdateNotes::class.java))
        }

        layoutAddContacts.setOnClickListener {
            startActivity(Intent(this, AddContacts::class.java))
        }
    }
}
