package com.example.saleemacademy

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itextpdf.kernel.colors.Color
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import org.w3c.dom.Document as Document1

class NurseryMarks: AppCompatActivity() {
    private lateinit var btnLoadStudents: Button
    private lateinit var btnDownloadList: Button
    private lateinit var btnSave : Button
    private lateinit var btnLoadSavedList : Button
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerTerm: Spinner
    private lateinit var tableLayout: TableLayout
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurserymarks)

        btnLoadStudents = findViewById(R.id.btnLoadStudents)
        btnDownloadList = findViewById(R.id.btnDownload)
        btnSave  = findViewById(R.id.btnSave)
        btnLoadSavedList = findViewById(R.id.btnLoadSavedList)
        tableLayout = findViewById(R.id.tableLayout)
        storageRef = FirebaseStorage.getInstance().reference.child("csv/student_data.csv")



        storage = FirebaseStorage.getInstance()

        btnLoadSavedList.setOnClickListener{
            showLoadSavedListDialog()
        }

        btnLoadStudents.setOnClickListener {
            showLoadStudentsDialog()
        }
        btnSave.setOnClickListener{
            showSaveDialog()
        }
        btnDownloadList.setOnClickListener{
            showDownloadDialog()
        }

        requestStoragePermission()
        createNotificationChannel()
    }
    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    NurseryMarks.WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(NurseryStudents.CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NurseryStudents.WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Storage permission is required to download data", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showExamTypeDialog(year: String, term: String, mode: DialogMode) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_exam_type, null)
        val radioGroup = dialogView.findViewById<RadioGroup>(R.id.radioGroup)
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Select Exam Type")
            .setPositiveButton("OK") { _, _ ->
                val selectedId = radioGroup.checkedRadioButtonId
                val selectedRadioButton = dialogView.findViewById<RadioButton>(selectedId)
                val examType = selectedRadioButton.text.toString()
                when (mode) {
                    DialogMode.SAVE -> saveStudents(year, term, examType)
                    DialogMode.LOAD -> fetchSavedListData(year, term, examType)
                    DialogMode.DOWNLOAD ->   downloadStudents(year, term, examType)
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.show()
    }
    private enum class DialogMode {
        SAVE,
        LOAD,
        DOWNLOAD
    }



    private fun showDownloadDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save, null)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.spinnerYear)
        val termSpinner = dialogView.findViewById<Spinner>(R.id.spinnerTerm)
        val years = arrayOf("2022", "2023", "2024", "2025")
        val terms = arrayOf("Term 1", "Term 2", "Term 3")
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
        val termAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        termSpinner.adapter = termAdapter

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Download Data")
            .setPositiveButton("Download") { _, _ ->
                val year = years[yearSpinner.selectedItemPosition]
                val term = terms[termSpinner.selectedItemPosition]
                showExamTypeDialog(year, term, DialogMode.DOWNLOAD)

            }
            .setNegativeButton("Cancel") { _, _ -> }

        dialogBuilder.show()
    }

    private fun downloadStudents(year: String, term: String, examType: String) {
        val fileName = "${javaClass.simpleName}_$year" + "_$term" + "_$examType.csv"
        val fileRef = storageRef.child(fileName)

        fileRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val data = String(bytes)
                createAndSavePdf(year, term, examType, data)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to download data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun createAndSavePdf(year: String, term: String, examType: String, data: String) {
        requestStoragePermission()

        val pdfFileName = "${javaClass.simpleName}_$year" + "_$term" + "_$examType.pdf"
        val pdfFilePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.absolutePath + File.separator + pdfFileName
        Log.d("PDF", "PDF File Path: $pdfFilePath")

        try {
            val outputStream = FileOutputStream(pdfFilePath)
            val writer = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)
            val document = Document(pdfDocument, PageSize.A4)

            val appBackgroundColor = DeviceRgb(255, 193, 7) // Replace with your color code (FFC107)
            val textColor: Color = DeviceRgb(255, 193, 7)// Color for text

            val header = Paragraph("MERU SALEEM ACADEMY").setBold().setFontSize(18f).setMarginBottom(20f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(header)

            val className = Paragraph("Nursery Class").setBold().setFontSize(16f).setMarginBottom(10f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(className)

            val yearTermExamType = "Year: $year, Term: $term, Exam Type: $examType"
            val headerInfo = Paragraph(yearTermExamType).setBold().setFontSize(14f).setMarginBottom(20f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(headerInfo)

            val studentList = Paragraph("Students Marks").setBold().setFontSize(14f).setMarginBottom(20f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(studentList)

            val table = Table(UnitValue.createPercentArray(floatArrayOf(8f, 12f, 8f, 8f, 8f, 8f, 8f, 8f, 8f))).useAllAvailableWidth()
            table.setHorizontalAlignment(HorizontalAlignment.CENTER)

            val snoTitleCell = Cell().add(Paragraph("S.No")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(snoTitleCell)

            val nameTitleCell = Cell().add(Paragraph("Student Name")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(nameTitleCell)

            val englishTitleCell = Cell().add(Paragraph("English")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(englishTitleCell)

            val kiswahiliTitleCell = Cell().add(Paragraph("Kiswahili")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(kiswahiliTitleCell)

            val mathTitleCell = Cell().add(Paragraph("Maths")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(mathTitleCell)

            val scienceTitleCell = Cell().add(Paragraph("Science")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(scienceTitleCell)

            val socialStudiesTitleCell = Cell().add(Paragraph("Social Studies")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(socialStudiesTitleCell)

            val creTitleCell = Cell().add(Paragraph("CRE")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(creTitleCell)

            val totalTitleCell = Cell().add(Paragraph("TOTAL")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(totalTitleCell)

            val rows = data.split("\n")

            for ((index, rowString) in rows.withIndex()) {
                if (index == 0) continue // Skip first row

                val columns = rowString.split(",")
                if (columns.size >= 8) {
                    val name = columns[0].trim()
                    val english = columns[1].trim()
                    val kiswahili = columns[2].trim()
                    val math = columns[3].trim()
                    val science = columns[4].trim()
                    val socialStudies = columns[5].trim()
                    val cre = columns[6].trim()
                    val total = columns[7].trim()

                    val numberedName = "${index}"

                    val snoCell = Cell().add(Paragraph(numberedName))
                    val nameCell = Cell().add(Paragraph(name))
                    val englishCell = Cell().add(Paragraph(english))
                    val kiswahiliCell = Cell().add(Paragraph(kiswahili))
                    val mathCell = Cell().add(Paragraph(math))
                    val scienceCell = Cell().add(Paragraph(science))
                    val socialStudiesCell = Cell().add(Paragraph(socialStudies))
                    val creCell = Cell().add(Paragraph(cre))
                    val totalCell = Cell().add(Paragraph(total))

                    table.addCell(snoCell)
                    table.addCell(nameCell)
                    table.addCell(englishCell)
                    table.addCell(kiswahiliCell)
                    table.addCell(mathCell)
                    table.addCell(scienceCell)
                    table.addCell(socialStudiesCell)
                    table.addCell(creCell)
                    table.addCell(totalCell)
                }
            }

            document.add(table)

            val footer = Paragraph("Powered by D_M tech Solutions\nEmail us: murerwadenis55@gmail.com").setFontSize(10f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.showTextAligned(footer, PageSize.A4.width / 2, document.bottomMargin + 10, TextAlignment.CENTER)

            document.close()

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
                        this.setDataAndType(FileProvider.getUriForFile(this@NurseryMarks, applicationContext.packageName + ".provider", file), "application/pdf")
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



    private fun showLoadSavedListDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_load_students, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Load Saved List")
            .setView(dialogView)

        val btnLoadSavedList = dialogView.findViewById<Button>(R.id.btnLoad)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        spinnerYear = dialogView.findViewById(R.id.spinnerYear)
        spinnerTerm = dialogView.findViewById(R.id.spinnerTerm)
        val dialog = dialogBuilder.create()

        btnLoadSavedList.setOnClickListener {
            val year = spinnerYear.selectedItem.toString()
            val term = spinnerTerm.selectedItem.toString()
            showExamTypeDialog(year, term, DialogMode.LOAD)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchSavedListData(year: String, term: String, examType: String) {
        val fileName = "${javaClass.simpleName}_$year" + "_$term" + "_$examType.csv"
        val fileRef = storageRef.child(fileName)

        fileRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val savedListData = String(bytes)
            val rows = savedListData.split("\n")
            if (rows.size > 1) {
                val savedListData = String(bytes)
                displaySavedList(savedListData)
            } else {
                Log.d("NurseryMarks", "No students found or only header found.")
            }
        }.addOnFailureListener {
            Toast.makeText(this, "The List you selected does not exist", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displaySavedList(savedListData: String) {
        tableLayout.removeViews(1, tableLayout.childCount - 1)

        val rows = savedListData.split("\n").map { it.trim() }
        if (rows.size > 1) {
            // Add the data rows
            for (row in rows.drop(1)) {
                val columns = row.split(",")
                if (columns.isNotEmpty()) {
                    val studentName = columns[0]
                    Log.d("NurseryMarks", "Processing student: $studentName")

                    val studentRow = TableRow(this)
                    studentRow.tag = studentName // Set tag to identify the row

                    // Create student name TextView
                    val studentTextView = TextView(this)
                    studentTextView.text = studentName
                    studentTextView.setPadding(8, 8, 8, 8)
                    studentTextView.setBackgroundResource(R.drawable.table_border)
                    studentTextView.gravity = Gravity.CENTER
                    val layoutParams = TableRow.LayoutParams(
                        0,
                        TableRow.LayoutParams.WRAP_CONTENT,
                        3.0f
                    ) // Adjusted width to WRAP_CONTENT and weight to 1.0
                    studentTextView.layoutParams = layoutParams
                    studentRow.addView(studentTextView)

                    // Add EditTexts for subjects
                    val subjectsCount = columns.size - 2 // Adjusted to exclude the student name and total columns
                    val subjectEditTexts = mutableListOf<EditText>()
                    for (i in 1 until columns.size - 1) {
                        val subjectEditText = EditText(this)
                        subjectEditText.text = Editable.Factory.getInstance().newEditable(columns[i])
                        subjectEditText.isFocusable = false // Prevent editing of loaded data
                        subjectEditText.isClickable = false
                        subjectEditText.setPadding(8, 8, 8, 8)
                        subjectEditText.setBackgroundResource(R.drawable.table_border)
                        val subjectParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT)
                        subjectParams.weight = 1f
                        subjectEditText.layoutParams = subjectParams
                        subjectEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER // Only accept numbers
                        subjectEditTexts.add(subjectEditText)
                        studentRow.addView(subjectEditText)
                    }

                    // Total column TextView
                    val totalTextView = TextView(this)
                    totalTextView.setPadding(8, 8, 8, 8)
                    totalTextView.setBackgroundResource(R.drawable.table_border)
                    totalTextView.gravity = Gravity.CENTER
                    totalTextView.text = columns.last() // Set total marks from the data
                    val totalParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f)
                    totalParams.weight = 1f
                    totalTextView.layoutParams = totalParams
                    studentRow.addView(totalTextView)

                    // Long click listener for edit and delete options
                    studentRow.setOnLongClickListener {
                        val dialogBuilder = AlertDialog.Builder(this)
                        dialogBuilder.setTitle("Options")
                        dialogBuilder.setItems(arrayOf("Edit", "Delete")) { dialog, which ->
                            when (which) {
                                0 -> handleEdit(columns)
                                1 -> handleDelete(columns)
                            }
                            dialog.dismiss()
                        }
                        dialogBuilder.show()
                        true
                    }

                    tableLayout.addView(studentRow)
                }
            }
        } else {
            Log.d("NurseryMarks", "No data to display or only headers found.")
        }
    }

    private fun handleEdit(columns: List<String>) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Edit Student")

        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(50, 20, 50, 20)

        // Create and add EditTexts for each subject with headers
        val editTexts = mutableListOf<Pair<String, EditText>>() // Pair of subject name and corresponding EditText
        val headerRow = tableLayout.getChildAt(0) as TableRow
        for (i in 1 until columns.size - 1) {
            val subjectName = (headerRow.getChildAt(i) as TextView).text.toString()
            val editText = EditText(this)
            editText.hint = subjectName
            editText.inputType = InputType.TYPE_CLASS_NUMBER

            // Populate EditText with existing values
            editText.setText(columns[i])

            editText.layoutParams = layoutParams
            layout.addView(editText)
            editTexts.add(Pair(subjectName, editText))
        }

        dialogBuilder.setView(layout)

        dialogBuilder.setPositiveButton("Update") { dialog, _ ->
            val updatedData = mutableListOf<String>()
            updatedData.add(columns[0]) // Keep the student name unchanged

            var allFieldsValid = true
            for ((_, editText) in editTexts) {
                val value = editText.text.toString().trim()
                if (value.isEmpty()) {
                    editText.error = "Field cannot be empty"
                    allFieldsValid = false
                } else {
                    updatedData.add(value)
                }
            }

            if (allFieldsValid) {
                // Update the row with the new data
                updateTableRow(columns, updatedData)

                // Calculate the new total marks after editing
                val studentRow = tableLayout.findViewWithTag<TableRow>(columns[0])
                val totalMarks = calculateTotalMarks(studentRow, editTexts.map { it.second })

                // Update the total marks displayed in the row
                updateTotalMarks(studentRow, totalMarks)

                dialog.dismiss()
                Toast.makeText(this, "Marks updated successfully", Toast.LENGTH_SHORT).show()
            }
        }

        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        dialogBuilder.show()
    }



    private fun handleDelete(columns: List<String>) {
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Delete Student")
            .setMessage("Are you sure you want to delete this student?")
            .setPositiveButton("Delete") { dialog, _ ->
                val index = tableLayout.indexOfChild(tableLayout.findViewWithTag<View>(columns[0]))
                tableLayout.removeViewAt(index)
                dialog.dismiss()
                Toast.makeText(this, "Student deleted successfully", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }

        dialogBuilder.show()
    }

    private fun updateTableRow(columns: List<String>, updatedData: List<String>) {
        val studentRow = tableLayout.findViewWithTag<TableRow>(columns[0])
        if (studentRow != null) {
            for (i in 1 until columns.size - 1) {
                val editText = studentRow.getChildAt(i) as EditText
                editText.setText(updatedData[i])
            }
            val totalTextView = studentRow.getChildAt(studentRow.childCount - 1) as TextView
            totalTextView.text = updatedData.last()
        } else {
            Log.e("NurseryMarks", "Student row not found with tag: ${columns[0]}")
        }
    }




    private fun showSaveDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_save, null)
        val yearSpinner = dialogView.findViewById<Spinner>(R.id.spinnerYear)
        val termSpinner = dialogView.findViewById<Spinner>(R.id.spinnerTerm)
        val years = arrayOf("2022", "2023", "2024", "2025") // Example years
        val terms = arrayOf("Term 1", "Term 2", "Term 3") // Example terms
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        yearSpinner.adapter = yearAdapter
        val termAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, terms)
        termAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        termSpinner.adapter = termAdapter

        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("Save Data")
            .setPositiveButton("Save") { _, _ ->
                val year = years[yearSpinner.selectedItemPosition]
                val term = terms[termSpinner.selectedItemPosition]
                showExamTypeDialog(year, term, DialogMode.SAVE)

            }
            .setNegativeButton("Cancel") { _, _ -> }

        dialogBuilder.show()
    }


    private fun saveStudents(year: String, term: String, examType: String) {
        val studentDataList = mutableListOf<String>()
        var hasEmptyCells = false

        val headerRow = tableLayout.getChildAt(0) as TableRow
        val header = mutableListOf<String>()
        for (i in 1 until headerRow.childCount - 1) {
            header.add((headerRow.getChildAt(i) as TextView).text.toString())
        }
        val headerString = "Student Name,${header.joinToString(",")},Total Marks"

        for (i in 1 until tableLayout.childCount) {
            val studentRow = tableLayout.getChildAt(i) as TableRow
            val studentName = (studentRow.getChildAt(0) as TextView).text.toString()
            val totalMarks = (studentRow.getChildAt(studentRow.childCount - 1) as TextView).text.toString()
            val subjectMarks = mutableListOf<String>()
            var hasEmptyMarks = false
            for (j in 1 until studentRow.childCount - 1) {
                val subjectMark = (studentRow.getChildAt(j) as EditText).text.toString()
                if (subjectMark.isEmpty()) {
                    hasEmptyMarks = true
                    break
                }
                subjectMarks.add(subjectMark)
            }
            if (studentName.isEmpty() || totalMarks.isEmpty() || hasEmptyMarks) {
                hasEmptyCells = true
                continue
            }
            val studentData = "$studentName,${subjectMarks.joinToString(",")},$totalMarks"
            studentDataList.add(studentData)
        }

        if (studentDataList.size <= 0) {
            Toast.makeText(this, "No data to save", Toast.LENGTH_SHORT).show()
            return
        }

        // Sort studentDataList based on total marks (assuming total marks are at the end)
        studentDataList.sortByDescending {
            val columns = it.split(",")
            columns.last().toIntOrNull() ?: 0
        }

        // Insert header string at the beginning
        studentDataList.add(0, headerString)

        if (hasEmptyCells) {
            Toast.makeText(this, "Some rows have empty cells. Data not saved.", Toast.LENGTH_SHORT).show()
            return
        }

        val data = studentDataList.joinToString("\n")
        val fileName = "${javaClass.simpleName}_$year" + "_$term" + "_$examType.csv"
        val storageRef = FirebaseStorage.getInstance().reference.child("csv/student_data.csv/$fileName")
        storageRef.putBytes(data.toByteArray())
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun showLoadStudentsDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_load_students, null)
        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Load Students")
            .setView(dialogView)

        val btnLoad = dialogView.findViewById<Button>(R.id.btnLoad)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        spinnerYear = dialogView.findViewById(R.id.spinnerYear)
        spinnerTerm = dialogView.findViewById(R.id.spinnerTerm)
        val dialog = dialogBuilder.create()

        btnLoad.setOnClickListener {
            val selectedYear = spinnerYear.selectedItem.toString()
            val selectedTerm = spinnerTerm.selectedItem.toString()

            val path = "NurseryStudents_${selectedYear}_${selectedTerm}.csv"


            fetchStudentsData(path)
            dialog.dismiss()
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun fetchStudentsData(path: String) {
        storageRef.child("$path").getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val studentsData = String(bytes)
            val students = studentsData.split("\n").map { it.trim() }
            if (students.size > 1) {
                val studentNames = students.drop(1).map { it.split(",")[0] }
                displayStudents(studentNames)
            } else if (students.size == 1 && students[0].isNotBlank()) {
                val studentNames = students.map { it.split(",")[0] }
                displayStudents(studentNames)
            } else {

                Log.d("NurseryMarks", "No students found or only header found.")
            }
        }.addOnFailureListener {

        }
    }


    private fun displayStudents(studentNames: List<String>) {
        val headerRow = tableLayout.getChildAt(0) as TableRow

        // Clear the table layout except for the header row
        tableLayout.removeAllViews()
        tableLayout.addView(headerRow)

        // Set layout params for student name title
        val studentNameTitleParams = headerRow.getChildAt(0).layoutParams

        for ((index, studentName) in studentNames.withIndex()) {
            if (studentName.isNotEmpty()) {
                Log.d("NurseryMarks", "Processing student ${index + 1}: $studentName")

                val studentRow = TableRow(this)

                val studentTextView = TextView(this)
                studentTextView.text = studentName
                studentTextView.setPadding(8, 8, 8, 8)
                studentTextView.setBackgroundResource(R.drawable.table_border)
                studentTextView.gravity = Gravity.CENTER
                studentTextView.layoutParams = studentNameTitleParams
                studentRow.addView(studentTextView)

                // Add EditTexts for subjects
                val subjectsCount = headerRow.childCount - 2 // Adjusted to exclude the total column
                val subjectEditTexts = mutableListOf<EditText>()
                for (i in 0 until subjectsCount) {
                    val subjectEditText = EditText(this)
                    subjectEditText.text = Editable.Factory.getInstance().newEditable("")
                    subjectEditText.setPadding(8, 8, 8, 8)
                    subjectEditText.setBackgroundResource(R.drawable.table_border)
                    val subjectParams = TableRow.LayoutParams(0, studentNameTitleParams.height)
                    subjectParams.weight = 1f
                    subjectEditText.layoutParams = subjectParams
                    subjectEditText.inputType = android.text.InputType.TYPE_CLASS_NUMBER // Only accept numbers
                    subjectEditText.setOnFocusChangeListener { view, hasFocus ->
                        if (!hasFocus) {
                            val marksText = (view as EditText).text.toString()
                            if (marksText.isNotEmpty()) {
                                val marks = marksText.toIntOrNull() ?: 0 // If marksText cannot be parsed to int, default to 0
                                if (marks > 100) {
                                    Toast.makeText(this, "Marks awarded should be 100 or below", Toast.LENGTH_SHORT).show()
                                    view.setText("100")
                                }
                                val totalMarks = calculateTotalMarks(studentRow, subjectEditTexts)
                                updateTotalMarks(studentRow, totalMarks)
                            }
                        }
                    }
                    subjectEditTexts.add(subjectEditText)
                    studentRow.addView(subjectEditText)
                }

                // Total column TextView
                val totalTextView = TextView(this)
                totalTextView.setPadding(8, 8, 8, 8)
                totalTextView.setBackgroundResource(R.drawable.table_border)
                totalTextView.gravity = Gravity.CENTER
                totalTextView.text = "0" // Initially total marks are 0
                totalTextView.layoutParams = TableRow.LayoutParams(0, studentNameTitleParams.height, 1f)
                studentRow.addView(totalTextView)

                tableLayout.addView(studentRow)
            } else {
                Log.d("NurseryMarks", "Skipping empty student at index ${index + 1}")
            }
        }
    }

    private fun calculateTotalMarks(studentRow: TableRow, subjectEditTexts: List<EditText>): Int {
        var totalMarks = 0
        for (editText in subjectEditTexts) {
            val marksText = editText.text.toString()
            if (marksText.isNotEmpty()) {
                val marks = marksText.toIntOrNull() ?: 0 // If marksText cannot be parsed to int, default to 0
                totalMarks += marks.coerceAtMost(100) // Limit marks to 100
            }
        }
        return totalMarks
    }

    private fun updateTotalMarks(studentRow: TableRow, totalMarks: Int) {
        val totalTextView = studentRow.getChildAt(studentRow.childCount - 1) as TextView
        totalTextView.text = totalMarks.toString()
    }

    companion object {
        private const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001
        private const val CHANNEL_ID = "pdf_download_channel"
    }
}
