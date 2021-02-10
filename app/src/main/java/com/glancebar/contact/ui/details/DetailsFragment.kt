package com.glancebar.contact.ui.details

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.AddContactActivity
import com.glancebar.contact.AddContactActivity.Companion.CONTACT_ID
import com.glancebar.contact.MainActivity
import com.glancebar.contact.R
import com.glancebar.contact.databinding.DetailsFragmentBinding
import com.glancebar.contact.enums.NavigationDetailsEnum
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.glancebar.contact.persistence.repository.ContactRepository
import com.glancebar.contact.persistence.repository.HistoryRepository
import com.glancebar.contact.ui.contacts.ActivityResult
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
    private lateinit var historyRecyclerView: RecyclerView
    private var contactDao: ContactDao = AppDatabase.INSTANCE!!.getContactDao()
    private var contactRepository = ContactRepository(AppDatabase.INSTANCE!!)
    private var historyRepository = HistoryRepository(AppDatabase.INSTANCE!!)
    private lateinit var binding: DetailsFragmentBinding
    private lateinit var returnCallback: OnBackPressedCallback
    private var favoriteItem: MenuItem? = null
    private var histories: MutableList<History> = mutableListOf()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
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
        binding.contact = Contact()
        initRecyclerView(binding.root)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpAdapter()
        loadContactAndHistory(args.contactId)
        setListener()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.favorite_contact -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    if (binding.contact!!.isMarked == 1) {
                        binding.contact!!.isMarked = 0
                        contactRepository.favorite(binding.contact!!)
                        Toast.makeText(
                            context,
                            getString(R.string.unfavoriated),
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        favoriteItem?.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_like)
                    } else {
                        binding.contact!!.isMarked = 1
                        contactRepository.favorite(binding.contact!!)
                        Toast.makeText(context, getString(R.string.favoriated), Toast.LENGTH_SHORT)
                            .show()
                        favoriteItem?.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_liked)
                    }
                }
            }
            R.id.edit_contact -> {
                val intent = Intent(context, AddContactActivity::class.java)
                intent.putExtra(CONTACT_ID, binding.contact!!.id)
                startActivityForResult(intent, ActivityResult.ADD_CONTACT.code)
            }
            R.id.delete_contact -> {
                val alertDialog: AlertDialog = requireActivity().let {
                    val builder = AlertDialog.Builder(it)
                    builder.setMessage(getString(R.string.sure_to_delete)).setTitle(R.string.alert)
                    builder.apply {
                        setPositiveButton(
                            R.string.ok
                        ) { dialog, id ->
                            contactRepository.delete(contact = binding.contact!!)
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.delete_success),
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateBack()
                        }
                        setNegativeButton(
                            R.string.cancel
                        ) { dialog, id ->
                            // User cancelled the dialog
                        }
                    }
                    builder.create()
                }
                alertDialog.show()
                return true
            }
            else -> {
                if (navigateBack()) return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun navigateBack(): Boolean {
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
        return false
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.details_menu, menu)
        favoriteItem = menu.findItem(R.id.favorite_contact)
        // TODO: 初始化时机不知道对不对
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
        binding.clearHistoryLabel.setOnClickListener {
            val alertDialog: AlertDialog = requireActivity().let {
                val builder = AlertDialog.Builder(it)
                builder.setMessage(getString(R.string.sure_to_clear_history))
                    .setTitle(R.string.alert)
                builder.apply {
                    setPositiveButton(
                        R.string.ok
                    ) { dialog, id ->
                        historyRepository.clearContactHistory(binding.contact!!.number!!)
                        histories.clear()
                        loadContactAndHistory(args.contactId)
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.cleared_history),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    setNegativeButton(
                        R.string.cancel
                    ) { dialog, id ->
                        // User cancelled the dialog
                    }
                }
                builder.create()
            }
            alertDialog.show()
        }
    }


    private fun setUpAdapter() {
        historyRecyclerView.adapter =
            HistoryAdapter(
                histories,
                binding.contact,
                object : OnRecyclerReachBottomListener {
                    override fun onBottomReached(position: Int) {

                    }
                },
                historyRepository
            )
    }


    private fun initRecyclerView(view: View) {
        historyRecyclerView = view.findViewById(R.id.contact_detail_history_recycler)
        historyRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun setContact(contact: Contact) {
        val data = binding.contact
        data?.username = contact.username
        data?.avatar = contact.avatar
        data?.email = contact.email
        data?.telephone = contact.telephone
        data?.isMarked = contact.isMarked
        data?.location = contact.location
        data?.tags = contact.tags
        data?.number = contact.number
    }

    private fun loadContactAndHistory(contactId: Long) {
        viewLifecycleOwner.lifecycleScope.launch {
            Log.d("Collect error", "loadContactAndHistory: $contactId")
            contactDao.getAllContactsAndHistoryByName(contactId).collect { it ->
                it?.contact?.also {
                    setContact(it)
                    if (it.avatar == null) {
                        binding.contactDetailsAvatar.setImageResource(Consts.DEFAULT_AVATAR)
                    } else {
                        Glide.with(requireView()).load(it.avatar).into(binding.contactDetailsAvatar)
                    }
                    if (it.isMarked == 1) {
                        favoriteItem?.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_liked)
                    }
                    binding.contactDetailsNumber.text = it.number!!
                    val numberInfo = PhoneUtil.getPhoneModel(it.number!!.replace(" ", ""))
                    if (numberInfo == null) {
                        binding.contactDetailsInfo.text = "未知号码"
                    } else {
                        binding.contactDetailsInfo.text =
                            PhoneUtil.getPhoneModel(it.number!!.replace(" ", "")).toString()
                    }
                }
                histories.clear()
                histories.addAll(it!!.histories)
                historyRecyclerView.adapter?.notifyDataSetChanged()
            }
        }
    }
}


class HistoryAdapter(
    private val histories: MutableList<History>,
    private val contact: Contact?,
    private val onRecyclerReachBottomListener: OnRecyclerReachBottomListener,
    private val historyRepository: HistoryRepository
) : RecyclerView.Adapter<HistoryAdapter.HistoryItemViewHolder>() {

    class HistoryItemViewHolder(
        private val view: View,
        private val histories: MutableList<History>,
        val contact: Contact?,
        private var adapter: HistoryAdapter,
        private var historyRepository: HistoryRepository
    ) : RecyclerView.ViewHolder(view) {
        private var historyListItem: ConstraintLayout = view.findViewById(R.id.history_list_item)
        private var historyAvatarView: ImageView = view.findViewById(R.id.history_contact_avatar)
        private var contactNameView: TextView = view.findViewById(R.id.history_contact_username)
        private var contactNumberView: TextView =
            view.findViewById(R.id.history_contact_number_info)
        private var deleteHistoryImageView: ImageView = view.findViewById(R.id.history_delete)
        private var historyTypeView: ImageView = view.findViewById(R.id.history_type)

        fun setData(history: History) {
            contactNameView.text = contact?.username
            contactNumberView.text = contact?.number
            historyListItem.setOnClickListener {
                val uri = "tel:${contact?.number}"
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
                view.context.startActivity(intent)
            }
            // avatar
            Glide.with(view).load(contact?.avatar).into(historyAvatarView)
            deleteHistoryImageView.setOnClickListener {
                // TODO: delete and remove
                histories.remove(history)
                adapter.notifyDataSetChanged()
                historyRepository.clearHistory(history)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.history_list_item, parent, false)
        return HistoryItemViewHolder(view, histories, contact, this, historyRepository)
    }

    override fun onBindViewHolder(viewHolder: HistoryItemViewHolder, position: Int) {
        if (position == histories.size - 1) {
            onRecyclerReachBottomListener.onBottomReached(position)
        }

        viewHolder.setData(histories[position])
    }

    override fun getItemCount(): Int {
        return histories.size
    }
}


