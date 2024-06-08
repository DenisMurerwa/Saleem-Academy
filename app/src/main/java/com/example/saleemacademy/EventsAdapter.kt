import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.saleemacademy.R

class EventsAdapter(private val eventTitles: List<String>, private val onItemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(title: String)
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(title: String) {
            titleTextView.text = title
            itemView.setOnClickListener {
                onItemClickListener.onItemClick(title)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.event_item, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(eventTitles[position])
    }

    override fun getItemCount(): Int {
        return eventTitles.size
    }
}
