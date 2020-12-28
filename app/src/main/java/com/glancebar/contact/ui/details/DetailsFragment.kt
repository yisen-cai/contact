package com.glancebar.contact.ui.details

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.MainActivity
import com.glancebar.contact.R
import com.glancebar.contact.databinding.DetailsFragmentBinding
import com.glancebar.contact.enums.NavigationDetailsEnum
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.glancebar.contact.persistence.repository.ContactRepository
import com.glancebar.contact.utils.Consts
import com.glancebar.contact.utils.OnRecyclerReachBottomListener
import com.glancebar.contact.utils.PhoneUtil
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class DetailsFragment : Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private val args: DetailsFragmentArgs by navArgs()
    private lateinit var viewModel: DetailsViewModel
    private lateinit var historyRecyclerView: RecyclerView
    private var contactDao: ContactDao = AppDatabase.INSTANCE!!.getContactDao()
    private var contactRepository = ContactRepository(AppDatabase.INSTANCE!!)
    private lateinit var binding: DetailsFragmentBinding
    private lateinit var returnCallback: OnBackPressedCallback


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This callback will only be called when MyFragment is at least Started.
//        returnCallback = requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().navigate(R.id.detail_back_to_history)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).hideNavigator()
        binding = DataBindingUtil.inflate(inflater, R.layout.details_fragment, container, false)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        binding.contact = viewModel.contact.value
        initRecyclerView(binding.root)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        loadContactAndHistory(args.contactNumber)
        setListener()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (args.sourceFragment) {
            NavigationDetailsEnum.CONTACTS -> {
                findNavController().navigate(R.id.detail_back_to_contact)
                return true
            }
            NavigationDetailsEnum.FAVORITE -> {
                findNavController().navigate(R.id.detail_back_to_favorite)
            }
            NavigationDetailsEnum.HISTORY -> {
                findNavController().navigate(R.id.detail_back_to_history)
            }
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("WrongConstant")
    private fun setListener() {
        binding.contactDetailsCall.setOnClickListener {
            val uri = "tel:${binding.contact?.number}"
            val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
            startActivity(intent)
        }
        binding.contactDetailsMessage.setOnClickListener {
            val uri = "sms:${binding.contact?.number}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }

        // TODO: not update
        binding.favoriteContact.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                if (binding.contact!!.isMarked == 1) {
                    binding.contact!!.isMarked = 0
                    contactRepository.update(binding.contact!!)
                    Toast.makeText(context, getString(R.string.unfavoriated), Toast.LENGTH_SHORT)
                        .show()
                    binding.favoriteContact.setImageResource(R.drawable.ic_like)
                } else {
                    binding.contact!!.isMarked = 1
                    contactRepository.update(binding.contact!!)
                    Toast.makeText(context, getString(R.string.favoriated), Toast.LENGTH_SHORT)
                        .show()
                    binding.favoriteContact.setImageResource(R.drawable.ic_liked)
                }
            }
        }

        binding.editContact.setOnClickListener {

        }
    }


    private fun setUpAdapter() {
        historyRecyclerView.adapter =
            HistoryAdapter(
                viewModel.histories.value!!,
                viewModel.contact.value!!,
                object : OnRecyclerReachBottomListener {
                    override fun onBottomReached(position: Int) {

                    }
                }
            )
    }


    private fun initRecyclerView(view: View) {
        historyRecyclerView = view.findViewById(R.id.contact_detail_history_recycler)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun loadContactAndHistory(number: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            contactDao.getAllContactsAndHistory(number).collect { it ->
                it?.contact?.also {
                    binding.contact = it
                    if (it.avatar == null) {
                        binding.contactDetailsAvatar.setImageResource(Consts.DEFAULT_AVATAR)
                    } else {
                        Glide.with(requireView()).load(it.avatar).into(binding.contactDetailsAvatar)
                    }
                    if (it.isMarked == 1) {
                        binding.favoriteContact.setImageResource(R.drawable.ic_liked)
                    }
                    val numberInfo = PhoneUtil.getPhoneModel(it.number!!)
                    if (numberInfo == null) {
                        binding.contactDetailsInfo.text = "未知号码"
                    } else {
                        binding.contactDetailsInfo.text =
                            PhoneUtil.getPhoneModel(it.number!!).toString()
                    }
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
    private val contact: Contact,
    private val onRecyclerReachBottomListener: OnRecyclerReachBottomListener
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
            Glide.with(view).load(contact.avatar).into(historyAvatarView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return HistoryItemViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        if (position == histories.size - 1) {
            onRecyclerReachBottomListener.onBottomReached(position)
        }

        viewHolder.setData(histories[position], contact)
    }

    override fun getItemCount(): Int {
        return histories.size
    }
}


