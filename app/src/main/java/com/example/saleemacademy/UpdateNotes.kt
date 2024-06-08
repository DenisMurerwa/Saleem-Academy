package com.example.saleemacademy

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import com.google.firebase.storage.FirebaseStorage
class UpdateNotes: AppCompatActivity() {

    private lateinit var selectedFilePath: String
    private lateinit var selectedFileTitle: String
    private val storage = FirebaseStorage.getInstance()

    private val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedFilePath = it.toString()

            Toast.makeText(this, "File selected successfully!", Toast.LENGTH_SHORT).show()
            // You may want to show some UI indication that a file is selected
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.updatenotes)
        findViewById<Button>(R.id.selectButton).setOnClickListener {
            openFilePicker()
        }

        findViewById<Button>(R.id.previewButton).setOnClickListener {
            retrieveFilesFromFirebaseStorage()
        }

        findViewById<Button>(R.id.uploadButton).setOnClickListener {
            if (::selectedFilePath.isInitialized) {
                showTitleInputDialog()
            } else {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun retrieveFilesFromFirebaseStorage() {
        val storageRef = storage.reference.child("Notes")

        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                val files = listResult.items
                val fileNames = mutableListOf<String>()


                for (file in files) {
                    fileNames.add(file.name)
                }


                showFilesDialog(fileNames)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to retrieve files: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showFilesDialog(fileNames: List<String>) {

        val dialogView = LayoutInflater.from(this).inflate(R.layout.pastpapers_dialog, null)


        val listViewFiles: ListView = dialogView.findViewById(R.id.listViewFiles)


        val fileListAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileNames)
        listViewFiles.adapter = fileListAdapter


        listViewFiles.setOnItemClickListener { _, _, position, _ ->
            val fileName = fileNames[position]
            showDeleteFileDialog(fileName)
        }


        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setTitle("Files")
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun showDeleteFileDialog(fileName: String) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Delete File")
        dialogBuilder.setMessage("Are you sure you want to delete $fileName?")

        dialogBuilder.setPositiveButton("Delete") { _, _ ->
            deleteFileFromFirebaseStorage(fileName)
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun deleteFileFromFirebaseStorage(fileName: String) {
        val storageRef = storage.reference.child("Notes").child(fileName)

        storageRef.delete()
            .addOnSuccessListener {
                Toast.makeText(this, "File deleted successfully!", Toast.LENGTH_SHORT).show()
                retrieveFilesFromFirebaseStorage()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to delete file: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openFilePicker() {
        filePickerLauncher.launch("application/pdf")
    }


    private fun showTitleInputDialog() {
        val builder = AlertDialog.Builder(this)
        val input = EditText(this)
        input.hint = "Enter Title"
        builder.setView(input)

        builder.setTitle("Enter Title")
        builder.setPositiveButton("OK") { _, _ ->
            val title = input.text.toString()
            if (title.isNotEmpty()) {
                selectedFileTitle = title
                uploadFileToFirebaseStorage()
            } else {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun uploadFileToFirebaseStorage() {

        if (selectedFilePath.isEmpty()) {
            Toast.makeText(this, "No file selected!", Toast.LENGTH_SHORT).show()
            return
        }
        val storageRef = storage.reference
        val fileRef = storageRef.child("Notes/$selectedFileTitle.pdf")
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading...")
        progressDialog.show()

        val uploadTask = fileRef.putFile(selectedFilePath.toUri())

        uploadTask.addOnProgressListener { taskSnapshot ->
            val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
            progressDialog.setMessage("Uploaded $progress%")
        }

        uploadTask.addOnCompleteListener { task ->
            progressDialog.dismiss()
            if (task.isSuccessful) {
                Toast.makeText(this, "File uploaded successfully!", Toast.LENGTH_SHORT).show()
                notifyNotesFragment()
            } else {
                Toast.makeText(this, "Failed to upload file: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun notifyNotesFragment() {
        // Notify NotesFragment to refresh its data
        val intent = Intent("com.example.saleemacademy.REFRESH_NOTES")
        sendBroadcast(intent)
    }
}