package com.example.saleemacademy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Grade1Attendance: AppCompatActivity() {
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var selectedTerm: String
    private lateinit var selectedDate: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.grade1_attendance)
        storage = FirebaseStorage.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("csv/student_data.csv")
        val tvAssignment = findViewById<TextView>(R.id.tvAssignments)
        val btnDownload = findViewById<Button>(R.id.buttonDownload)

        showLoadDialog()

        tvAssignment.setOnClickListener {
            showSubjectSelectionDialog()
        }

        btnDownload.setOnClickListener {
            showYearTermDateDialog()
        }

        val saveButton = findViewById<Button>(R.id.buttonSave)
        saveButton.setOnClickListener {
            saveAttendance()
        }

        createNotificationChannel()
    }

    private fun showLoadDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_load_students, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Load Students")
            .setView(dialogView)

        val btnLoad = dialogView.findViewById<Button>(R.id.btnLoad)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val spinnerYear = dialogView.findViewById<Spinner>(R.id.spinnerYear)
        val spinnerTerm = dialogView.findViewById<Spinner>(R.id.spinnerTerm)
        val dialog = dialogBuilder.create()

        btnLoad.setOnClickListener {
            val selectedYear = spinnerYear.selectedItem.toString()
            selectedTerm = spinnerTerm.selectedItem.toString()
            val path = "Grade1Students_${selectedYear}_${selectedTerm}.csv"
            loadFileFromStorage(path)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadFileFromStorage(path: String) {
        storageRef.child(path).getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val studentsData = String(bytes)
            Log.d("Grade1Attendance", "Students data loaded successfully: $studentsData")
            val studentLines = studentsData.split("\n").map { it.trim() }
            replaceStudentNames(studentLines)
            uncheckAllCheckboxes()
        }.addOnFailureListener {
            Log.e("Grade1Attendance", "Error loading file from storage: ${it.message}")
        }
    }

    private fun replaceStudentNames(studentLines: List<String>) {
        val layout = findViewById<LinearLayout>(R.id.linearStudentList) // Linear layout to hold the list

        val appBackgroundColor = resources.getColor(R.color.app_background)

        layout.removeAllViews() // Remove all views before adding new ones

        studentLines.forEachIndexed { index, line ->
            // Skip header line
            if (index == 0) return@forEachIndexed

            val studentData = line.split(",")
            if (studentData.size >= 2) {
                val name = studentData[0]
                val newCheckBox = CheckBox(this)
                newCheckBox.text = name
                newCheckBox.textSize = 20f
                newCheckBox.setTextColor(appBackgroundColor)
                layout.addView(newCheckBox)
            }
        }
    }

    private fun uncheckAllCheckboxes() {
        val layout = findViewById<LinearLayout>(R.id.linearStudentList)
        for (i in 0 until layout.childCount) {
            val view = layout.getChildAt(i)
            if (view is CheckBox) {
                view.isChecked = false
            }
        }
    }

    private fun saveAttendance() {
        val studentDataString = getStudentDataAsString()

        if (studentDataString.isNotEmpty()) {
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val path = "Grade1Attendance_${selectedTerm}_$selectedDate.csv" // Include selected term and date
            val attendanceRef = storageRef.child(path)

            try {
                val outputStream = ByteArrayOutputStream()
                outputStream.write(studentDataString.toByteArray())

                attendanceRef.putBytes(outputStream.toByteArray())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Attendance saved successfully", Toast.LENGTH_SHORT).show()
                        Log.d("NurseryAttendance", "Attendance saved successfully")
                        uncheckAllCheckboxes()
                    }
                    .addOnFailureListener { e ->
                        Log.e("NurseryAttendance", "Error saving attendance: ${e.message}")
                    }
            } catch (e: IOException) {
                Log.e("NurseryAttendance", "Error writing to output stream: ${e.message}")
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getStudentDataAsString(): String {
        val layout = findViewById<LinearLayout>(R.id.linearStudentList)
        val outputStream = ByteArrayOutputStream()

        try {
            for (i in 0 until layout.childCount) {
                val view = layout.getChildAt(i)
                if (view is CheckBox) {
                    val status = if (view.isChecked) "Present" else "Absent"
                    val studentName = view.text.toString()
                    outputStream.write("$studentName,$status\n".toByteArray())
                }
            }
            return outputStream.toString()
        } catch (e: IOException) {
            Log.e("NurseryAttendance", "Error converting student data to string: ${e.message}")
            return ""
        }
    }

    private fun showYearTermDateDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_year, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Select Term and Date")
            .setView(dialogView)

        val spinnerTerm = dialogView.findViewById<Spinner>(R.id.spinnerTerm)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val btnSelect = dialogView.findViewById<Button>(R.id.btnSelect)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val dialog = dialogBuilder.create()

        btnSelect.setOnClickListener {
            selectedTerm = spinnerTerm.selectedItem.toString()
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year - 1900 // Date uses 1900 as the base year
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(year, month, day))

            // Load the student data for the selected date and term
            val path = "Grade1Attendance_${selectedTerm}_$selectedDate.csv"
            Log.d("NurseryAttendance", "Loading file from path: $path")
            loadFileFromStorageForPdf(path)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun loadFileFromStorageForPdf(path: String) {
        storageRef.child(path).getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val attendanceData = String(bytes)
            Log.d("NurseryAttendance", "Attendance data loaded successfully: $attendanceData")
            val studentLines = attendanceData.split("\n").map { it.trim() }
            createAndDownloadAttendancePdf(studentLines, selectedTerm, selectedDate)
        }.addOnFailureListener {
            Log.e("NurseryAttendance", "Error loading file from storage: ${it.message}")
        }
    }

    private fun createAndDownloadAttendancePdf(
        studentLines: List<String>,
        term: String,
        selectedDate: String
    ) {
        val pdfFileName = "Grade1Attendance_${term}_$selectedDate.pdf"
        val downloadsDir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val pdfFile = File(downloadsDir, pdfFileName)
        val outputStream = FileOutputStream(pdfFile)

        val writer = PdfWriter(outputStream)
        val pdfDocument = PdfDocument(writer)
        val document = Document(pdfDocument)
        pdfDocument.defaultPageSize = PageSize.A4
        document.setMargins(20f, 20f, 20f, 20f)

        val header = Paragraph("Grade1 Attendance - Term: $term, Date: $selectedDate")
            .setBold()
            .setFontSize(18f)
            .setTextAlignment(TextAlignment.CENTER)
            .setFontColor(DeviceRgb(0, 102, 204))
        document.add(header)

        document.add(Paragraph("\n"))

        val table = Table(UnitValue.createPercentArray(floatArrayOf(4f, 4f)))
        table.useAllAvailableWidth()
        table.setHorizontalAlignment(HorizontalAlignment.CENTER)

        // Add table headers
        table.addCell(Cell().add(Paragraph("Student Name").setBold()))
        table.addCell(Cell().add(Paragraph("Attendance Status").setBold()))

        studentLines.forEach { line ->
            val studentData = line.split(",")
            if (studentData.size >= 2) {
                val studentName = studentData[0]
                val attendanceStatus = studentData[1]
                table.addCell(Cell().add(Paragraph(studentName)))
                table.addCell(Cell().add(Paragraph(attendanceStatus)))
            }
        }

        document.add(table)
        document.close()

        showNotification(pdfFileName, pdfFile)
    }

    private fun showNotification(pdfFileName: String, pdfFile: File) {
        val intent = Intent(Intent.ACTION_VIEW)
        val pdfUri = FileProvider.getUriForFile(this, "$packageName.provider", pdfFile)
        intent.setDataAndType(pdfUri, "application/pdf")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setContentTitle("Grade 1 Attendance")
            .setContentText("Attendance PDF created: $pdfFileName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(0, notificationBuilder.build())

        Toast.makeText(this, "PDF saved in Downloads", Toast.LENGTH_SHORT).show()
    }
    private fun showSubjectSelectionDialog() {
        val subjects = listOf("English", "Kiswahili", "Science", "Social Studies", "CRE", "Mathematics")
        val selectedSubjects = mutableListOf<String>()

        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_select_subjects, null)
        val layout = dialogView.findViewById<LinearLayout>(R.id.subjectSelectionLayout)

        subjects.forEach { subject ->
            val checkBox = CheckBox(this).apply {
                text = subject
                textSize = 18f
            }
            layout.addView(checkBox)
        }

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Select Subjects")
            .setView(dialogView)
            .setPositiveButton("Done") { dialog, _ ->
                for (i in 0 until layout.childCount) {
                    val view = layout.getChildAt(i)
                    if (view is CheckBox && view.isChecked) {
                        selectedSubjects.add(view.text.toString())
                    }
                }
                saveSelectedSubjects(selectedSubjects)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun saveSelectedSubjects(subjects: List<String>) {
        if (subjects.isNotEmpty()) {
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val path = "Grade1Assignments_$selectedDate.csv"
            val assignmentsRef = storageRef.child(path)

            val subjectData = subjects.joinToString("\n") { it }

            try {
                val outputStream = ByteArrayOutputStream()
                outputStream.write(subjectData.toByteArray())

                assignmentsRef.putBytes(outputStream.toByteArray())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Assignments saved successfully", Toast.LENGTH_SHORT).show()
                        Log.d("NurseryAttendance", "Assignments saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NurseryAttendance", "Error saving assignments: ${e.message}")
                    }
            } catch (e: IOException) {
                Log.e("NurseryAttendance", "Error writing to output stream: ${e.message}")
            }
        } else {
            Toast.makeText(this, "No subjects selected", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val CHANNEL_ID = "NurseryAttendanceChannel"
    }
}
