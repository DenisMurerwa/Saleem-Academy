// NotesFragment.kt
package com.example.saleemacademy

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage

class NotesFragment : Fragment() {

    private val storage = FirebaseStorage.getInstance()
    private lateinit var notesAdapter: NotesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        val recyclerViewNotes = view.findViewById<RecyclerView>(R.id.recyclerViewNotes)
        recyclerViewNotes.layoutManager = LinearLayoutManager(context)
        notesAdapter = NotesAdapter(listOf())
        recyclerViewNotes.adapter = notesAdapter

        retrieveFilesFromFirebaseStorage()

        // Register broadcast receiver to refresh notes
        val filter = IntentFilter("com.example.saleemacademy.REFRESH_NOTES")
        requireActivity().registerReceiver(refreshNotesReceiver, filter)

        return view
    }

    private fun retrieveFilesFromFirebaseStorage() {
        val storageRef = storage.reference.child("Notes")
        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val files = listResult.items
                val fileNames = files.map { it.name }
                notesAdapter.updateNotes(fileNames)
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

    private val refreshNotesReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            retrieveFilesFromFirebaseStorage()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().unregisterReceiver(refreshNotesReceiver)
    }
}
