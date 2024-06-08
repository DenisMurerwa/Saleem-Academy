package com.example.saleemacademy

import MyAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)

        val images = listOf(R.drawable.trends, R.drawable.machogu)
        val titles = listOf("Kids & Technology", "Schools To be Closed..")


        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)


        val adapter = MyAdapter(requireContext(), images, titles)


        recyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = adapter

        return rootView
    }

override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    auth = FirebaseAuth.getInstance()

    val tvAdmin: TextView = view.findViewById(R.id.tvAdmin)
    val tvFees: TextView = view.findViewById(R.id.tvFees)
    val tvPastPapers: TextView = view.findViewById(R.id.tvPastPapers)
    val tvAll: TextView = view.findViewById(R.id.tvAll)
    val tvEvents: TextView = view.findViewById(R.id.tvEvents)


    tvAdmin.setOnClickListener {
        val currentUser = auth.currentUser
        if (currentUser != null && currentUser.email == "saleemacademy@gmail.com") {
            startActivity(Intent(requireContext(), Admin::class.java))
        } else {
            Toast.makeText(requireContext(), "You are not authorized to access this section", Toast.LENGTH_SHORT).show()
        }
    }

    tvFees.setOnClickListener {
        startActivity(Intent(requireContext(), Fees::class.java))
    }

    tvPastPapers.setOnClickListener {
        startActivity(Intent(requireContext(), PastPapers::class.java))
    }

    tvAll.setOnClickListener {
        startActivity(Intent(requireContext(), All::class.java))
    }

    tvEvents.setOnClickListener {
        startActivity(Intent(requireContext(), Events::class.java))
         }
    }
}