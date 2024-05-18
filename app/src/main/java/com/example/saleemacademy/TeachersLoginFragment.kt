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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.concurrent.atomic.AtomicInteger

class TeachersLoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginButton: Button
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teachers_login2, container, false)
        email = view.findViewById(R.id.username)
        password = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.login)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        loginButton.setOnClickListener {
            val email = email.text.toString()
            val password = password.text.toString()

            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        if (user != null) {
                            checkIfEmailIsAllowed(user)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "User is null",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun checkIfEmailIsAllowed(user: FirebaseUser) {

        val userEmail = user.email

        if (userEmail != null) {
            val gradesCount = 8

            val successCount = AtomicInteger(0)
            for (gradeIndex in 0 until gradesCount) {
                val gradeRef = storageRef.child("emails/grade$gradeIndex")
                gradeRef.getBytes(Long.MAX_VALUE)
                    .addOnSuccessListener { bytes ->
                        val gradeEmails = String(bytes, Charsets.UTF_8)
                        if (userEmail in gradeEmails) {

                            val intent = Intent(requireContext(), Teacherslogin::class.java)
                            startActivity(intent)
                            requireActivity().finish()
                            return@addOnSuccessListener
                        } else {

                            successCount.incrementAndGet()

                            if (successCount.get() == gradesCount) {
                                Toast.makeText(
                                    requireContext(),
                                    "Access Denied",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to retrieve emails: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Email is null",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}
