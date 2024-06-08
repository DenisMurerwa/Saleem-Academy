package com.example.saleemacademy

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*

class StudentsProgress : AppCompatActivity() {

    private lateinit var storageRef: StorageReference
    private lateinit var studentClass: String
    private lateinit var studentName: String
    private lateinit var studentNumber: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var assignmentsAdapter: AssignmentsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.students_progress)

        storageRef = FirebaseStorage.getInstance().reference

        studentClass = intent.getStringExtra("studentClass").toString()
        studentName = intent.getStringExtra("studentName").toString()
        studentNumber = intent.getStringExtra("studentNumber").toString()

        val btnAssignments = findViewById<Button>(R.id.button2)
        val btnEndTermReport = findViewById<Button>(R.id.button3)
        recyclerView = findViewById(R.id.recyclerView)

        assignmentsAdapter = AssignmentsAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = assignmentsAdapter

        btnAssignments.setOnClickListener {
            Log.d("StudentsProgress", "Load Assignments button clicked")
            showDatePickerDialog()
        }

        btnEndTermReport.setOnClickListener {
            Log.d("StudentsProgress", "End Term Report button clicked")
            showReportOptionsDialog()
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, year, month, dayOfMonth ->
            val selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(
                GregorianCalendar(year, month, dayOfMonth).time
            )
            loadAssignments(selectedDate)
        }, year, month, dayOfMonth)
        datePicker.show()
    }

    private fun loadAssignments(date: String) {
        val path = "${studentClass}Assignments_$date.csv"
        Log.d("StudentsProgress", "Path: $path")

        loadFile(path, "Assignments loaded successfully") { data ->
            displayAssignmentsData(data)
        }
    }

    private fun showReportOptionsDialog() {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_report_options)

        val spinnerYear: Spinner = dialog.findViewById(R.id.spinnerYear)
        val spinnerTerm: Spinner = dialog.findViewById(R.id.spinnerTerm)
        val spinnerExamType: Spinner = dialog.findViewById(R.id.spinnerExamType)
        val btnLoadReport: Button = dialog.findViewById(R.id.btnLoadReport)

        val years = arrayOf("2022", "2023", "2024", "2025")
        val terms = arrayOf("Term 1", "Term 2", "Term 3")
        val examTypes = arrayOf("Opener Exam", "Midterm Exam", "Endterm Exam")

        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        val termAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        val examTypeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, examTypes)

        spinnerYear.adapter = yearAdapter
        spinnerTerm.adapter = termAdapter
        spinnerExamType.adapter = examTypeAdapter

        btnLoadReport.setOnClickListener {
            val selectedYear = spinnerYear.selectedItem.toString()
            val selectedTerm = spinnerTerm.selectedItem.toString()
            val selectedExamType = spinnerExamType.selectedItem.toString()

            val fileName = "${studentClass}Marks_$selectedYear" + "_$selectedTerm" + "_$selectedExamType.csv"
            Log.d("StudentsProgress", "Selected File: $fileName")

            loadFile(fileName, "End of Term Report loaded successfully") { data ->
                displayReportData(data)
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadFile(fileName: String, successMessage: String, onDataLoaded: (String) -> Unit) {
        val fileRef = storageRef.child(fileName)

        fileRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val data = String(bytes, StandardCharsets.UTF_8)
                Log.d("StudentsProgress", successMessage)
                onDataLoaded(data)
            }
            .addOnFailureListener { exception ->
                Log.e("StudentsProgress", "Failed to load file '$fileName': ${exception.message}")
                Toast.makeText(this, "Failed to load file: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayAssignmentsData(data: String) {
        val filteredData = filterDataForStudent(data)
        assignmentsAdapter.updateData("Assignments", filteredData)
    }

    private fun displayReportData(data: String) {
        val filteredData = filterDataForStudent(data)
        assignmentsAdapter.updateData("End of Term Report", filteredData)
    }

    private fun filterDataForStudent(data: String): String {
        val reader = BufferedReader(InputStreamReader(data.byteInputStream(StandardCharsets.UTF_8)))
        var line: String?
        val result = StringBuilder()
        while (reader.readLine().also { line = it } != null) {
            val parts = line?.split(",")?.map { it.trim() }
            if (parts != null && parts.size >= 2 && parts[0] == studentName && parts[1] == studentNumber) {
                result.append(line).append("\n")
            }
        }
        return result.toString()
    }
}
