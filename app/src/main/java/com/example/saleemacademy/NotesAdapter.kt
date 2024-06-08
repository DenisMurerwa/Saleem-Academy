// NotesAdapter.kt
package com.example.saleemacademy

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage

class NotesAdapter(private var notes: List<String>) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    private val storage = FirebaseStorage.getInstance()

    inner class NoteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val noteTitle: TextView = view.findViewById(R.id.textFileName)
        val downloadButton: Button = view.findViewById(R.id.buttonDownload)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_item, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notes[position]
        holder.noteTitle.text = note

        holder.downloadButton.setOnClickListener {
            downloadFile(holder.itemView.context, note)
        }
    }

    override fun getItemCount(): Int {
        return notes.size
    }

    fun updateNotes(newNotes: List<String>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    private fun downloadFile(context: Context, fileName: String) {
        val storageRef = storage.reference.child("Notes").child(fileName)
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val intent = Intent(Intent.ACTION_VIEW, uri)
            context.startActivity(intent)
        }.addOnFailureListener {
            // Handle error
        }
    }
}
