package com.example.saleemacademy

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Grade1 : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.grade1)

        val btnMarks = findViewById<Button>(R.id.button_marks)
        val btnAttendance = findViewById<Button>(R.id.button_attendance)
        val btnAddStudents = findViewById<Button>(R.id.btnAddStudents)


        btnMarks.setOnClickListener{

            val intent = Intent(this,Grade1Marks::class.java)
            startActivity(intent)
        }
        btnAttendance.setOnClickListener{
            val intent = Intent(this,Grade1Attendance::class.java)
            startActivity(intent)
        }
        btnAddStudents.setOnClickListener(){
            val intent = Intent(this,Grade1Students::class.java)
            startActivity(intent)
        }
    }
}