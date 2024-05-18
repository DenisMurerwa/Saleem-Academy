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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.saleemacademy.NurseryStudents.Companion.CHANNEL_ID
import com.example.saleemacademy.NurseryStudents.Companion.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
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

class NurseryAttendance: AppCompatActivity() {

    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var selectedTerm: String
    private lateinit var selectedDate: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurseryattedance)

        storage = FirebaseStorage.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("csv/student_data.csv")
        val tvAssignment = findViewById<TextView>(R.id.tvAssignments)
        val tvRemarks = findViewById<TextView>(R.id.tvRemarks)
        val btnDownload = findViewById<Button>(R.id.buttonDownload)

        showLoadDialog()


        tvAssignment.setOnClickListener {
            val intent = Intent(this, Assignments::class.java)
            startActivity(intent)
        }
        tvRemarks.setOnClickListener {
            val intent = Intent(this, Remarks::class.java)
            startActivity(intent)
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
            val path = "NurseryStudents_${selectedYear}_${selectedTerm}.csv"
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
            Log.d("NurseryAttendance", "Students data loaded successfully: $studentsData")
            val studentLines = studentsData.split("\n").map { it.trim() }
            replaceStudentNames(studentLines)
            // Uncheck all checkboxes after loading
            uncheckAllCheckboxes()
        }.addOnFailureListener {
            // Handle failure
            Log.e("NurseryAttendance", "Error loading file from storage: ${it.message}")
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

            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val path = "NurseryAttendance_${selectedTerm}_$selectedDate.csv" // Include selected term and date
            val attendanceRef = storageRef.child(path)

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
            .setTitle("Select Year, Term, and Date")
            .setView(dialogView)

        val spinnerYear = dialogView.findViewById<Spinner>(R.id.spinnerYear)
        val spinnerTerm = dialogView.findViewById<Spinner>(R.id.spinnerTerm)
        val datePicker = dialogView.findViewById<DatePicker>(R.id.datePicker)
        val btnSelect = dialogView.findViewById<Button>(R.id.btnSelect)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val dialog = dialogBuilder.create()

        btnSelect.setOnClickListener {
            val selectedYear = spinnerYear.selectedItem.toString()
            selectedTerm = spinnerTerm.selectedItem.toString()
            val day = datePicker.dayOfMonth
            val month = datePicker.month
            val year = datePicker.year
            selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(year - 1900, month, day))

            // Pass the selected year, term, date, and student data directly to the createAndDownloadAttendancePdf function
            createAndDownloadAttendancePdf(selectedYear, selectedTerm, selectedDate)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }





    private fun createAndDownloadAttendancePdf(
        year: String,
        term: String,
        selectedDate: String,
    ) {

        val studentData = getStudentDataAsString()
        Log.d("NurseryAttendance", "Student data for PDF generation: $studentData")
        val pdfFileName = "${javaClass.simpleName}" + "_$term" + "_$selectedDate.pdf"
        val pdfFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator + pdfFileName
        Log.d("PDF", "PDF File Path: $pdfFilePath")

        try {
            val outputStream = FileOutputStream(pdfFilePath)
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument, PageSize.A4)

            val appBackgroundColor = DeviceRgb(255, 193, 7) // Replace with your color code (FFC107)
            val textColor: DeviceRgb = DeviceRgb(255, 193, 7)// Color for text

            val header = Paragraph("Saleem Academy").setBold().setFontSize(18f).setMarginBottom(20f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(header)

            val attendanceHeader = Paragraph("Nursery Class Attendance").setBold().setFontSize(16f).setMarginBottom(10f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(attendanceHeader)

            val yearTermDate = "Year: $year, Term: $term, Date: $selectedDate"
            val headerInfo = Paragraph(yearTermDate).setBold().setFontSize(14f).setMarginBottom(20f).setTextAlignment(
                TextAlignment.CENTER).setFontColor(textColor)
            document.add(headerInfo)

            // Creating the table
            val table = Table(UnitValue.createPercentArray(floatArrayOf(4f, 4f))).useAllAvailableWidth()
            table.setHorizontalAlignment(HorizontalAlignment.CENTER)

            val snoTitleCell = Cell().add(Paragraph("S.No")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(snoTitleCell)

            val studentNameTitleCell = Cell().add(Paragraph("Student Name")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(studentNameTitleCell)

            // Add student data to the table
            val studentData = getStudentDataAsString() // Fetch student data here
            val studentLines = studentData.split("\n")
            for ((index, line) in studentLines.withIndex()) {
                if (index == 0) continue // Skip header line

                val studentData = line.split(",")
                if (studentData.size >= 2) {
                    val studentName = studentData[0].trim()
                    val attendance = studentData[1].trim() // Assuming attendance is the second column

                    val snoCell = Cell().add(Paragraph("${index + 1}"))
                    table.addCell(snoCell)

                    val studentNameCell = Cell().add(Paragraph(studentName))
                    table.addCell(studentNameCell)

                    val attendanceCell = Cell().add(Paragraph(attendance))
                    table.addCell(attendanceCell)
                }
            }

            document.add(table)

            val footer = Paragraph("Powered by D_M tech Solutions\nEmail us: murerwadenis55@gmail.com").setFontSize(10f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.showTextAligned(footer, PageSize.A4.width / 2, document.bottomMargin + 10, TextAlignment.CENTER)

            document.close()

            // Show notification and toast for download
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Downloading PDF")
                .setContentText("Download in progress")
                .setSmallIcon(R.drawable.ic_download_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)

            val notificationId = 1
            notificationManager.notify(notificationId, builder.build())

            Toast.makeText(this, "Download Started.....", Toast.LENGTH_SHORT).show()

            val file = File(pdfFilePath)

            builder.setContentTitle("PDF Downloaded")
                .setContentText("Download complete")
                .setProgress(0, 0, false)
                .setOngoing(false)
                .setAutoCancel(true)
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(Intent.ACTION_VIEW).apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        this.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        this.setDataAndType(FileProvider.getUriForFile(this@NurseryAttendance, applicationContext.packageName + ".provider", file), "application/pdf")
                    } else {
                        this.setDataAndType(Uri.fromFile(file), "application/pdf")
                    }
                }, PendingIntent.FLAG_UPDATE_CURRENT))

            notificationManager.notify(notificationId, builder.build())

        } catch (e: Exception) {
            Log.e("PDF", "Error saving PDF: ${e.message}")
            Toast.makeText(this, "Failed to save PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



}