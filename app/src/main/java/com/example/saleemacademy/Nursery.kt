package com.example.saleemacademy

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class Nursery : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.nursery)

        val btnMarks = findViewById<Button>(R.id.button_marks)
        val btnAttendance = findViewById<Button>(R.id.button_attendance)
        val btnAddStudents = findViewById<Button>(R.id.btnAddStudents)


        btnMarks.setOnClickListener{

            val intent = Intent(this,NurseryMarks::class.java)
                startActivity(intent)
        }
        btnAttendance.setOnClickListener{
            val intent = Intent(this,NurseryAttendance::class.java)
            startActivity(intent)
        }
        btnAddStudents.setOnClickListener(){
            val intent = Intent(this,NurseryStudents::class.java)
            startActivity(intent)
        }
    }
}