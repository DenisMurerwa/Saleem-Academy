package com.example.saleemacademy

import MyAdapter
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {


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


    val tvAdmin: TextView = view.findViewById(R.id.tvAdmin)
    val tvFees: TextView = view.findViewById(R.id.tvFees)
    val tvPastPapers: TextView = view.findViewById(R.id.tvPastPapers)
    val tvAll: TextView = view.findViewById(R.id.tvAll)
    val tvEvents: TextView = view.findViewById(R.id.tvEvents)


    tvAdmin.setOnClickListener {
        startActivity(Intent(requireContext(), Admin::class.java))
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