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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicInteger

class TeachersLoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var storageRef: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_teachers_login2, container, false)
        emailEditText = view.findViewById(R.id.username)
        passwordEditText = view.findViewById(R.id.password)
        loginButton = view.findViewById(R.id.login)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                loginUser(email, password)
            } else {
                Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    user?.let {
                        checkIfEmailIsAllowed(it)
                    } ?: run {
                        Toast.makeText(requireContext(), "User is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Authentication failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkIfEmailIsAllowed(user: FirebaseUser) {
        val userEmail = user.email ?: return Toast.makeText(requireContext(), "Email is null", Toast.LENGTH_SHORT).show()

        val gradesCount = 8
        val successCount = AtomicInteger(0)

        for (gradeIndex in 0 until gradesCount) {
            val gradeRef = storageRef.child("emails/grade$gradeIndex")
            gradeRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val gradeEmails = String(bytes, Charsets.UTF_8)
                if (userEmail in gradeEmails) {
                    navigateToTeachersLogin()
                    return@addOnSuccessListener
                } else {
                    successCount.incrementAndGet()
                    if (successCount.get() == gradesCount) {
                        Toast.makeText(requireContext(), "Access Denied", Toast.LENGTH_SHORT).show()
                    }
                }
            }.addOnFailureListener { exception ->
                Toast.makeText(requireContext(), "Failed to retrieve emails: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToTeachersLogin() {
        val intent = Intent(requireContext(), Teacherslogin::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
