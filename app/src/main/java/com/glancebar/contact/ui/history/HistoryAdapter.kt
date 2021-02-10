package com.glancebar.contact.ui.history

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.R
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.glancebar.contact.utils.PhoneUtil


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
        private val historyListItem: ConstraintLayout = view.findViewById(R.id.history_list_item)
        private val avatarImageView: ImageView = view.findViewById(R.id.history_contact_avatar)
        private val contactNameTextView: TextView = view.findViewById(R.id.history_contact_username)
        private val numberDetailTextView: TextView =
            view.findViewById(R.id.history_contact_number_info)
        private val deleteImageButton: ImageView = view.findViewById(R.id.history_delete)
        private val calLogType: ImageView = view.findViewById(R.id.history_type)

        fun bind(history: History, contact: Contact?) {
            if (contact != null) {
                Glide.with(view).load(contact.avatar).into(avatarImageView)
                contactNameTextView.text = contact.username
                historyListItem.setOnClickListener {
                    val actions =
                        HistoryFragmentDirections.actionNavigationHistoryToNavigationDetails(
                            contactId = contact.id
                        )
                    it.findNavController().navigate(actions)
                }
            } else {
                contactNameTextView.text = history.number
                historyListItem.setOnClickListener {
                    val uri = "tel:${history.number}"
                    val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
//                intent.data = Uri.parse(uri)
                    view.context.startActivity(intent)
                }
            }
            numberDetailTextView.text = PhoneUtil.getPhoneModel(history.number!!).toString()
            if (!history.isCall) {
                calLogType.setImageResource(R.drawable.ic_phone_receive)
            }
            if (!history.isMissedCall) {
                calLogType.setImageResource(R.drawable.ic_phone_missed)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.history_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val contact = contactMap[histories[position].number]
        holder.bind(histories[position], contact)
    }

    override fun getItemCount(): Int {
        return histories.size
    }
}