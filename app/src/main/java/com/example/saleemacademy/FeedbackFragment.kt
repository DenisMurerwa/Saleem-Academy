package com.example.saleemacademy

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment

class FeedbackFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_feedback, container, false)

        val feedbackInput = view.findViewById<EditText>(R.id.feedbackInput)
        val sendButton = view.findViewById<Button>(R.id.sendButton)

        sendButton.setOnClickListener {
            val feedbackText = feedbackInput.text.toString()
            if (feedbackText.isNotEmpty()) {
                sendEmail(feedbackText)
            } else {
                Toast.makeText(context, "Please enter your feedback", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun sendEmail(feedbackText: String) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("murerwadenis55@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Feedback or Complaint")
            putExtra(Intent.EXTRA_TEXT, feedbackText)
        }
        try {
            startActivity(Intent.createChooser(emailIntent, "Send feedback using..."))
            Toast.makeText(context, "Sending...", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send feedback", Toast.LENGTH_SHORT).show()
        }
    }
}
