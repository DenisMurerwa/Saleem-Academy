package com.example.saleemacademy

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saleemacademy.R
import java.util.*

class PastPapersAdapter(
    private val context: Context,
    private var paperList: MutableList<String>,
    private val onDownloadClickListener: (String) -> Unit
) : RecyclerView.Adapter<PastPapersAdapter.ViewHolder>() {

    private var filteredList: MutableList<String> = mutableListOf()

    init {
        filteredList.addAll(paperList)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_past_paper, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fileName = filteredList[position]
        holder.textFileName.text = fileName

        holder.buttonDownload.setOnClickListener {
            onDownloadClickListener(fileName)
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    fun filter(query: String) {
        filteredList.clear()
        if (query.isEmpty()) {
            filteredList.addAll(paperList)
        } else {
            val searchText = query.toLowerCase(Locale.getDefault())
            paperList.forEach { fileName ->
                if (fileName.toLowerCase(Locale.getDefault()).contains(searchText)) {
                    filteredList.add(fileName)
                }
            }
        }
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textFileName: TextView = itemView.findViewById(R.id.textFileName)
        val buttonDownload: Button = itemView.findViewById(R.id.buttonDownload)
    }
}

