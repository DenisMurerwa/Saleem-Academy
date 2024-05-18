package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class StudentProgressFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etNumber: EditText
    private lateinit var btnLogin: Button
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_student_progress2, container, false)

        etName = view.findViewById(R.id.etName)
        etNumber = view.findViewById(R.id.etNumber)
        btnLogin = view.findViewById(R.id.btnLogin)

        storageRef = FirebaseStorage.getInstance().reference.child("csv/student_data.csv")

        btnLogin.setOnClickListener {
            val name = etName.text.toString().trim()
            val number = etNumber.text.toString().trim()
            login(name, number)
        }

        return view
    }

    private fun login(name: String, number: String) {

        val years = arrayOf("2022", "2023", "2024", "2025")
        val terms = arrayOf("Term 1", "Term 2", "Term 3")

        val tasks = mutableListOf<Task<ByteArray>>()

        for (year in years) {
            for (term in terms) {
                val fileName = "NurseryStudents_$year" + "_$term.csv"
                val fileRef = storageRef.child(fileName)
                val task = fileRef.getBytes(Long.MAX_VALUE)
                tasks.add(task)
            }
        }

        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener { taskList ->
                for (task in taskList) {
                    if (task.isSuccessful) {
                        val bytes = (task.result as ByteArray)
                        val data = String(bytes)
                        if (data.contains("$name, $number")) {
                           val intent = Intent(requireContext(), StudentsProgress::class.java)
                            startActivity(intent)
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                    }
                }
                // Login failed
                Toast.makeText(requireContext(), "Invalid name or admission number", Toast.LENGTH_SHORT).show()
                etName.text.clear()
                etNumber.text.clear()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to check login: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}