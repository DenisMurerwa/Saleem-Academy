package com.example.saleemacademy

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class UpdateEvents : AppCompatActivity() {

    private lateinit var eventTitleEditText: EditText
    private lateinit var eventBodyEditText: EditText
    private lateinit var btnSave: Button
    private lateinit var btnPreview: Button
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.updateevents)

        eventTitleEditText = findViewById(R.id.editTextEventTitle)
        eventBodyEditText = findViewById(R.id.editTextEventBody)
        btnSave = findViewById(R.id.btnSave)
        btnPreview = findViewById(R.id.btnPreview)

        storageReference = FirebaseStorage.getInstance().reference

        btnSave.setOnClickListener {
            saveEventToFirebaseStorage()
        }

        btnPreview.setOnClickListener {
            retrieveEventTitlesFromFirebaseStorage()
        }
    }

    private fun saveEventToFirebaseStorage() {
        val title = eventTitleEditText.text.toString().trim()
        val body = eventBodyEditText.text.toString().trim()

        if (title.isEmpty() || body.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val eventRef = storageReference.child("events/$title.txt")

        eventRef.putBytes(body.toByteArray())
            .addOnSuccessListener {
                Toast.makeText(this, "Event saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun retrieveEventTitlesFromFirebaseStorage() {
        val eventTitles = mutableListOf<String>()
        storageReference.child("events").listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    eventTitles.add(item.name.substringBeforeLast('.'))
                }
                showFilesDialog(eventTitles)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve event titles: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFilesDialog(eventTitles: List<String>) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Events")
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialogView = LayoutInflater.from(this).inflate(R.layout.pastpapers_dialog, null)
        val listViewFiles: ListView = dialogView.findViewById(R.id.listViewFiles)
        val fileListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, eventTitles)
        listViewFiles.adapter = fileListAdapter

        listViewFiles.setOnItemClickListener { _, _, position, _ ->
            val selectedTitle = eventTitles[position]
            showEventDetailsDialog(selectedTitle)
        }

        dialogBuilder.setView(dialogView)
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showEventDetailsDialog(title: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle(title)
        dialogBuilder.setMessage("Do you want to delete this event?")

        dialogBuilder.setPositiveButton("Delete") { _, _ ->
            deleteEventFromFirebaseStorage(title)
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun deleteEventFromFirebaseStorage(title: String) {
        storageReference.child("events/$title.txt").delete()
            .addOnSuccessListener {
                Toast.makeText(this, "Event deleted successfully!", Toast.LENGTH_SHORT).show()
                retrieveEventTitlesFromFirebaseStorage()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete event: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}


