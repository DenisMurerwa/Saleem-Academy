package com.example.saleemacademy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AssignmentsAdapter : RecyclerView.Adapter<AssignmentsAdapter.ViewHolder>() {

    private var title: String = ""
    private var data: String = ""

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvContent: TextView = itemView.findViewById(R.id.tvContent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_assignment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvTitle.text = title
        holder.tvContent.text = data
    }

    override fun getItemCount(): Int {
        return if (data.isEmpty()) 0 else 1
    }

    fun updateData(title: String, data: String) {
        this.title = title
        this.data = data
        notifyDataSetChanged()
    }
}
