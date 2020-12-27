package com.glancebar.contact.ui.details

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.MainActivity
import com.glancebar.contact.R
import com.glancebar.contact.databinding.DetailsFragmentBinding
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.*

class DetailsFragment : Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var viewModel: DetailsViewModel
    private lateinit var historyRecyclerView: RecyclerView
    private var contactDao: ContactDao = AppDatabase.INSTANCE!!.getContactDao()
    private lateinit var binding: DetailsFragmentBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadContactAndHistory(args.contactNumber)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).hideNavigator()
        val root = inflater.inflate(R.layout.details_fragment, container, false)
        binding = DataBindingUtil.setContentView(requireActivity(), R.layout.details_fragment)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        binding.contact = viewModel.contact.value
        initRecyclerView(root)
        setUpAdapter()
        return root
    }

    private fun setUpAdapter() {
        historyRecyclerView.adapter =
            HistoryAdapter(viewModel.histories.value!!, viewModel.contact!!.value!!)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun initRecyclerView(view: View) {
        historyRecyclerView = view.findViewById(R.id.contact_detail_history_recycler)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadContactAndHistory(number: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            contactDao.getAllContactsAndHistory(number).collect {
                it?.contact?.also {
                    viewModel.contact = MutableLiveData<Contact>().apply { value = it }
                }
                it?.histories?.forEach { history ->
                    viewModel.histories.value?.add(history)
                }
                historyRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}


class HistoryAdapter(
    private val histories: List<History>,
    private val contact: Contact
) : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {

    class HistoryItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private var historyAvatarView: ImageView = view.findViewById(R.id.history_contact_avatar)
        private var contactNameView: TextView = view.findViewById(R.id.history_contact_username)
        private var contactNumberView: TextView =
            view.findViewById(R.id.history_contact_number_info)
        private var historyTypeView: ImageView = view.findViewById(R.id.history_type)

        fun setData(history: History, contact: Contact) {
            contactNameView.text = contact.username
            contactNumberView.text = contact.number
            // avatar
            Glide.with(view).load("http://goo.gl/gEgYUd").into(historyAvatarView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        viewHolder.setData(histories[position], contact)
    }

    override fun getItemCount(): Int {
        return histories.size
    }

}