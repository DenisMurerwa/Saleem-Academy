package com.example.saleemacademy

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.HorizontalAlignment
import com.itextpdf.layout.property.TextAlignment
import com.itextpdf.layout.property.UnitValue
import org.bouncycastle.asn1.x500.style.RFC4519Style.description
import org.bouncycastle.asn1.x500.style.RFC4519Style.name
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream


class NurseryStudents: AppCompatActivity() {
    private lateinit var newStudentName: EditText
    private lateinit var newStudentNumber: EditText
    private lateinit var btnAdd: Button
    private lateinit var btnSave: Button
    private lateinit var btnDownload: Button
    private lateinit var tableLayout: TableLayout
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private var headersAdded = false
    private lateinit var searchBar: EditText
    private var isStudentsLoaded = false
    private var loadedStudentsList = mutableListOf<Pair<String, String>>()
    private var loadedFilePath: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nurserystudents)

        newStudentName = findViewById(R.id.newStudentName)
        newStudentNumber = findViewById(R.id.newStudentNumber)
        btnAdd = findViewById(R.id.btnAdd)
        btnSave = findViewById(R.id.btnSave)
        tableLayout = findViewById(R.id.tableLayout)


        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("csv/student_data.csv")

        val btnLoadStudents = findViewById<Button>(R.id.btnLoadStudents)
        val btnDownload = findViewById<Button>(R.id.btnDownload)

        searchBar = findViewById(R.id.searchBar)

        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isStudentsLoaded) {
                    filterLoadedStudents(s.toString())
                }
            }
        })



        btnLoadStudents.setOnClickListener {
            showLoadDialog()
        }
        btnDownload.setOnClickListener {
            showDownloadDialog()
        }

        btnAdd.setOnClickListener {
            val name = newStudentName.text.toString().trim()
            val number = newStudentNumber.text.toString().trim()
            addStudent(name, number)
            checkDuplicate(name, number)
        }

        btnSave.setOnClickListener {
            showSaveDialog()
        }

        if (!isHeadersAdded() && tableLayout.childCount == 0) {
            addHeaders()
            setHeadersAdded(true)
        }

        setupTable()
        requestStoragePermission()
        createNotificationChannel()


    }



    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE
                )
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                Toast.makeText(this, "Storage permission is required to download data", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun filterLoadedStudents(query: String) {
        val filteredList = loadedStudentsList.filter { pair ->
            pair.first.contains(query, ignoreCase = true) || pair.second.contains(query, ignoreCase = true)
        }
        displayLoadedStudents(filteredList)
    }

    private fun parseData(data: String): MutableList<Pair<String, String>> {
        val studentsList = mutableListOf<Pair<String, String>>()
        val rows = data.split("\n")
        for (rowString in rows) {
            val columns = rowString.split(",")
            if (columns.size >= 2) {
                val name = columns[0].trim()
                val number = columns[1].trim()
                studentsList.add(name to number)
            }
        }
        return studentsList
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
                downloadStudents(year, term)
            }
            .setNegativeButton("Cancel") { _, _ -> }

        dialogBuilder.show()
    }


    private fun downloadStudents(year: String, term: String) {
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            val fileName = "${javaClass.simpleName}_$year" + "_$term.csv"
            val fileRef = storageRef.child(fileName)

            fileRef.getBytes(Long.MAX_VALUE)
                .addOnSuccessListener { bytes ->
                    val data = String(bytes)
                    Log.d("downloadStudents", "Downloaded Data: $data")
                    createAndSavePdf(year, term, data)
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(this, "Failed to download data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                WRITE_EXTERNAL_STORAGE_REQUEST_CODE
            )
        }
    }


    private fun createAndSavePdf(year: String, term: String, data: String) {
        requestStoragePermission()

        val pdfFileName = "${javaClass.simpleName}_$year" + "_$term.pdf"
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

            val studentList = Paragraph("Students List").setBold().setFontSize(14f).setMarginBottom(20f).setTextAlignment(TextAlignment.CENTER).setFontColor(textColor)
            document.add(studentList)

            val table = Table(UnitValue.createPercentArray(floatArrayOf(10f, 60f, 30f))).useAllAvailableWidth()
            table.setHorizontalAlignment(HorizontalAlignment.CENTER)

            val snoTitleCell = Cell().add(Paragraph("S.No")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(snoTitleCell)

            val nameTitleCell = Cell().add(Paragraph("Student Name")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(nameTitleCell)

            val numberTitleCell = Cell().add(Paragraph("Admission Number")).setBold().setBackgroundColor(appBackgroundColor)
            table.addCell(numberTitleCell)

            val rows = data.split("\n")

            for ((index, rowString) in rows.withIndex()) {
                if (index == 0) continue // Skip first row

                val columns = rowString.split(",")
                if (columns.size >= 2) {
                    val name = columns[0].trim()
                    val number = columns[1].trim()

                    val numberedName = "${index}"

                    val snoCell = Cell().add(Paragraph(numberedName))
                    val nameCell = Cell().add(Paragraph(name))
                    val numberCell = Cell().add(Paragraph(number))

                    table.addCell(snoCell)
                    table.addCell(nameCell)
                    table.addCell(numberCell)
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
                        this.setDataAndType(FileProvider.getUriForFile(this@NurseryStudents, applicationContext.packageName + ".provider", file), "application/pdf")
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

    private fun showLoadDialog() {
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
            .setTitle("Load Data")
            .setPositiveButton("Load") { _, _ ->
                val year = years[yearSpinner.selectedItemPosition]
                val term = terms[termSpinner.selectedItemPosition]
                loadStudents(year, term)
            }
            .setNegativeButton("Cancel") { _, _ -> }

        dialogBuilder.show()
    }


    private fun loadStudents(year: String, term: String) {
        Log.d("loadStudents", "Loading students for year $year, term $term")

        val fileName = "${javaClass.simpleName}_$year" + "_$term.csv"
        val fileRef = storageRef.child(fileName)

        fileRef.getBytes(Long.MAX_VALUE)
            .addOnSuccessListener { bytes ->
                val data = String(bytes)
                Log.d("loadStudents", "Loaded Data: $data")
                loadedStudentsList = parseData(data)
                displayLoadedStudents(loadedStudentsList)

                loadedFilePath = fileName

                setupTable()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to load data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun setupTable() {
        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            row.setOnLongClickListener {
                showEditDeleteDialog(row) // Pass only the row to showEditDeleteDialog
                true
            }
        }
    }

    private fun showEditDeleteDialog(row: TableRow) {
        val rowIndex = tableLayout.indexOfChild(row)
        Log.d("RowIndex", "Row index: $rowIndex")

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit, null)
        val editStudentName = dialogView.findViewById<EditText>(R.id.editName)
        val editStudentNumber = dialogView.findViewById<EditText>(R.id.editNumber)

        val nameTextView = row.getChildAt(0) as TextView
        val numberTextView = row.getChildAt(1) as TextView
        val name = nameTextView.text.toString()
        val number = numberTextView.text.toString()
        editStudentName.setText(name)
        editStudentNumber.setText(number)

        val dialogBuilder = AlertDialog.Builder(this)
            .setTitle("Edit or Delete")
            .setView(dialogView)
            .setPositiveButton("Edit") { _, _ ->
                val editedName = editStudentName.text.toString()
                val editedNumber = editStudentNumber.text.toString()
                editRow(rowIndex, editedName, editedNumber)
            }
            .setNegativeButton("Delete") { _, _ ->
                markRowForDeletion(row, rowIndex)
            }
            .setCancelable(true)

        dialogBuilder.show()
    }
    private fun markRowForDeletion(row: TableRow, rowIndex: Int) {
        Log.d("ListSize", "List size before deletion: ${loadedStudentsList.size}")
        tableLayout.removeView(row)
        removeStudentFromList(rowIndex - 1) // Subtract 1 to account for headers row
        Log.d("ListSize", "List size after deletion: ${loadedStudentsList.size}")
    }
    private fun editRow(rowIndex: Int, newName: String, newNumber: String) {
        val row = tableLayout.getChildAt(rowIndex) as TableRow
        val nameTextView = row.getChildAt(0) as TextView
        val numberTextView = row.getChildAt(1) as TextView
        nameTextView.text = newName
        numberTextView.text = newNumber

        updateStudentInList(rowIndex - 1, newName, newNumber)
    }
    private fun updateStudentInList(index: Int, newName: String, newNumber: String) {
        loadedStudentsList[index] = newName to newNumber
    }

    private fun removeStudentFromList(index: Int) {
        loadedStudentsList.removeAt(index)
    }


    private fun addStudent(name: String, number: String) {

        if (name.isBlank() || number.isBlank()) {
            Toast.makeText(this, "Name and admission number cannot be empty", Toast.LENGTH_SHORT).show()
            return
        }
        if (checkDuplicate(name, number)) {
            Toast.makeText(this, "Duplicate entry found!", Toast.LENGTH_SHORT).show()
            return
        }

        val tableRow = TableRow(this)

        val nameTextView = TextView(this).apply {
            text = name
            setPadding(10, 10, 10, 10)
            setBackgroundResource(R.drawable.table_border)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3f)
        }

        val numberTextView = TextView(this).apply {
            text = number
            setPadding(10, 10, 10, 10)
            setBackgroundResource(R.drawable.table_border)
            layoutParams = TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2f)
        }

        tableRow.addView(nameTextView)
        tableRow.addView(numberTextView)

        tableLayout.addView(tableRow)

        newStudentName.text.clear()
        newStudentNumber.text.clear()
    }

    private fun displayLoadedStudents(data: List<Pair<String, String>>) {

        tableLayout.removeAllViews()

        for (student in data) {
            addStudent(student.first, student.second)
        }

        isStudentsLoaded = true
    }
    private fun checkDuplicate(name: String, number: String): Boolean {
        for (i in 1 until tableLayout.childCount) {
            val row = tableLayout.getChildAt(i) as TableRow
            val nameTextView = row.getChildAt(0) as TextView
            val numberTextView = row.getChildAt(1) as TextView
            val existingName = nameTextView.text.toString().trim()
            val existingNumber = numberTextView.text.toString().trim()
            if (name.equals(existingName, ignoreCase = true) && number.equals(existingNumber, ignoreCase = true)) {
                return true
            }
        }
        return false
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
                saveStudents(year, term)
            }
            .setNegativeButton("Cancel") { _, _ -> }

        dialogBuilder.show()
    }

    private fun saveStudents(year: String, term: String) {
        val fileName = "${javaClass.simpleName}_$year" + "_$term.csv"
        val fileRef = storageRef.child(fileName)

        val newData = buildUpdatedData(true)
        updateFile(fileRef, newData)
    }



    private fun buildUpdatedData(includeHeaders: Boolean): ByteArray {
        val outputStream = ByteArrayOutputStream()

        if (includeHeaders) {
            val headers = "Student Name, Admission Number\n".toByteArray()
            outputStream.write(headers)
        }

        outputStream.use { stream ->
            for (i in 1 until tableLayout.childCount) {
                val row = tableLayout.getChildAt(i) as TableRow
                val nameTextView = row.getChildAt(0) as TextView
                val numberTextView = row.getChildAt(1) as TextView
                val name = nameTextView.text.toString().trim()
                val number = numberTextView.text.toString().trim()

                if (name.isNotBlank() && number.isNotBlank() && name != "Student Name" && number != "Admission Number") {
                    val line = "$name, $number\n"
                    stream.write(line.toByteArray(Charsets.UTF_8))
                }
            }
        }

        return outputStream.toByteArray()
    }


    private fun updateFile(fileRef: StorageReference, newData: ByteArray) {
        fileRef.putBytes(newData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to save data: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun isHeadersAdded(): Boolean {
        val sharedPref = getPreferences(MODE_PRIVATE)
        return sharedPref.getBoolean("HeadersAdded", false)
    }

    private fun setHeadersAdded(value: Boolean) {
        val sharedPref = getPreferences(MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("HeadersAdded", value)
            apply()
        }
    }

    private fun addHeaders() {
        val headers = "Student Name, Admission Number\n".toByteArray()

        storageRef.putBytes(headers)
            .addOnSuccessListener {
                Toast.makeText(this, "Headers added successfully", Toast.LENGTH_SHORT).show()
                setHeadersAdded(true)
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Failed to add headers: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    companion object {
        const val WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 1001
        const val CHANNEL_ID = "pdf_download_channel"
    }
}
