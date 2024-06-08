package com.example.saleemacademy

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment

class LogoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_logout, container, false)


        showLogoutConfirmationDialog()

        return view
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Do you wish to logout?")
            .setPositiveButton("Yes") { dialog, id ->

                logoutUser()
            }
            .setNegativeButton("Cancel") { dialog, id ->

                dialog.dismiss()

                requireActivity().supportFragmentManager.popBackStack()
            }
        builder.create().show()
    }

    private fun logoutUser() {

        Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()


        val intent = Intent(activity, MainActivity::class.java)
        startActivity(intent)
        activity?.finish()
    }
}
