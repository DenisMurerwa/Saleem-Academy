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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

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
        val classes = arrayOf("Nursery", "Grade1", "Grade2", "Grade3", "Grade4", "Grade5", "Grade6", "Grade7", "Grade8")
        val years = arrayOf("2022", "2023", "2024", "2025")
        val terms = arrayOf("Term 1", "Term 2", "Term 3")

        var tasksRemaining = classes.size * years.size * terms.size

        for (year in years) {
            for (term in terms) {
                for (className in classes) {
                    val fileName = "${className}Students_${year}_${term}.csv"
                    val fileRef = storageRef.child(fileName)
                    fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                        val data = String(bytes, StandardCharsets.UTF_8)
                        if (checkStudentInData(data, name, number)) {
                            val intent = Intent(requireContext(), StudentsProgress::class.java)
                            intent.putExtra("studentClass", className)
                            intent.putExtra("studentName", name)
                            intent.putExtra("studentNumber", number)
                            startActivity(intent)
                            Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        tasksRemaining--
                        if (tasksRemaining == 0) {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Invalid name or admission number", Toast.LENGTH_SHORT).show()
                                etName.text.clear()
                                etNumber.text.clear()
                            }
                        }
                    }.addOnFailureListener { exception ->
                        tasksRemaining--
                        if (tasksRemaining == 0) {
                            activity?.runOnUiThread {
                                Toast.makeText(requireContext(), "Failed to check login: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkStudentInData(data: String, name: String, number: String): Boolean {
        val reader = BufferedReader(InputStreamReader(data.byteInputStream(StandardCharsets.UTF_8)))
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val parts = line?.split(",")?.map { it.trim() }
            if (parts != null && parts.size >= 2) {
                val studentName = parts[0]
                val studentNumber = parts[1]
                if (studentName == name && studentNumber == number) {
                    return true
                }
            }
        }
        return false
    }
}
