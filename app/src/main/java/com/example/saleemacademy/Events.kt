package com.example.saleemacademy

import EventsAdapter
import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class Events : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private val eventTitles = mutableListOf<String>()
    private val eventBodies = mutableMapOf<String, String>()
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.events)

        recyclerView = findViewById(R.id.recyclerViewEvents)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = EventsAdapter(eventTitles, object : EventsAdapter.OnItemClickListener {
            override fun onItemClick(title: String) {
                showEventBodyDialog(title)
            }
        })
        recyclerView.adapter = adapter

        storageReference = FirebaseStorage.getInstance().reference

        retrieveEventsFromFirebaseStorage()
    }

    private fun retrieveEventsFromFirebaseStorage() {
        storageReference.child("events").listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->
                    val title = item.name.substringBeforeLast('.')
                    eventTitles.add(title)
                    item.getBytes(Long.MAX_VALUE)
                        .addOnSuccessListener { bytes ->
                            val body = String(bytes)
                            eventBodies[title] = body
                            adapter.notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(this, "Failed to retrieve event: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to retrieve events: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showEventBodyDialog(title: String) {
        val body = eventBodies[title]
        if (body != null) {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.events_body_dialog, null)
            val bodyTextView: TextView = dialogView.findViewById(R.id.bodyTextView)
            bodyTextView.text = body

            val dialogBuilder = AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle(title)
                .setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }

            val dialog = dialogBuilder.create()
            dialog.show()
        } else {
            Toast.makeText(this, "Event body not found", Toast.LENGTH_SHORT).show()
        }
    }
}

