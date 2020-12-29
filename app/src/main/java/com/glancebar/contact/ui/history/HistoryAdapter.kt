package com.glancebar.contact.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.glancebar.contact.R
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History


/**
 *
 * @author Ethan Gary
 * @date 2020/12/29
 */
class HistoryAdapter(
    val histories: MutableList<History>,
    val contactMap: MutableMap<String, Contact>
) : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    class ViewHolder(
        val view: View
    ) : RecyclerView.ViewHolder(view) {

        private val avatarImageView: ImageView = view.findViewById(R.id.history_contact_avatar)
        private val contactNameTextView: TextView = view.findViewById(R.id.history_contact_username)
        private val numberDetailTextView: TextView =
            view.findViewById(R.id.history_contact_number_info)
        private val deleteImageButton: ImageButton = view.findViewById(R.id.history_delete)
        private val calLogType: ImageView = view.findViewById(R.id.history_type)

        fun bind() {
            // TODO
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return 0
    }
}