package com.example.saleemacademy

import android.Manifest
import android.app.DownloadManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File

class PastPapers : AppCompatActivity() {

    private val CHANNEL_ID = "download_channel"
    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1

    private lateinit var adapter: PastPapersAdapter
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.pastpapers)


        storageRef = FirebaseStorage.getInstance().reference.child("PastPapers")


        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPapers)
        recyclerView.layoutManager = LinearLayoutManager(this)


        val paperList: MutableList<String> = mutableListOf()
        val folderName = "PastPapers"


        storageRef.listAll()
            .addOnSuccessListener { listResult ->
                listResult.items.forEach { item ->

                    paperList.add(item.name)
                }


                Log.d("PastPapers", "Paper List: $paperList")


                adapter = PastPapersAdapter(this, paperList) { fileName ->
                    downloadFile(fileName)
                }

                recyclerView.adapter = adapter

                val searchEditText: EditText = findViewById(R.id.editTextText)
                searchEditText.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                    override fun afterTextChanged(s: Editable?) {
                        adapter.filter(s.toString())
                    }
                })
            }
            .addOnFailureListener { exception ->
                Log.e("PastPapers", "Failed to fetch paper list: ${exception.message}")

            }
    }

    private fun downloadFile(fileName: String) {

        val notificationManager = NotificationManagerCompat.from(this)
        createNotificationChannel(notificationManager)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Downloading $fileName")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setPriority(NotificationCompat.PRIORITY_LOW)

        val progressMax = 100
        builder.setProgress(progressMax, 0, false)

        try {
            // Check for WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Permission granted, proceed with downloading
                notificationManager.notify(fileName.hashCode(), builder.build())
                storageRef.child(fileName)
                    .downloadUrl
                    .addOnSuccessListener { uri ->
                        // Use DownloadManager to download the file
                        val downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
                        val request = DownloadManager.Request(uri)
                            .setTitle(fileName)
                            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)

                        downloadManager.enqueue(request)
                        // Cancel the notification when download is complete
                        notificationManager.cancel(fileName.hashCode())
                    }
                    .addOnFailureListener { exception ->
                        notificationManager.cancel(fileName.hashCode())
                        Log.e("PastPapers", "Failed to download file: ${exception.message}")
                        Toast.makeText(this, "Failed to download file", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Permission not granted, request it
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_WRITE_EXTERNAL_STORAGE
                )
            }
        } catch (e: SecurityException) {
            // Handle SecurityException
            e.printStackTrace()
            Log.e("PastPapers", "Security Exception: ${e.message}")
            Toast.makeText(this, "Security Exception: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun createNotificationChannel(notificationManager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Downloads"
            val descriptionText = "Download progress notifications"
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
