package com.glancebar.contact.ui.contacts

import android.app.Activity.RESULT_OK
import android.app.SearchManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.glancebar.contact.AddContactActivity
import com.glancebar.contact.MainActivity
import com.glancebar.contact.R
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.repository.ContactRepository
import com.glancebar.contact.utils.Consts
import com.glancebar.contact.utils.OnRecyclerReachBottomListener
import com.glancebar.contact.utils.notEmpty
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class ContactsFragment : Fragment() {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    private lateinit var viewModel: ContactsViewModel
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()
    private val contactRepository = ContactRepository()
    private lateinit var recyclerView: RecyclerView
    private var query: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        query = requireActivity().intent.getStringExtra(SearchManager.QUERY)

        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (requireActivity() as MainActivity).showNavigator()
        val root = inflater.inflate(R.layout.contacts_fragment, container, false)
        viewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        initRecyclerView(root)
        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadContactsFromDatabase()
    }

    private fun loadContactsFromDatabase() {

        viewLifecycleOwner.lifecycleScope.launch {
            if (query != null && notEmpty(query!!)) {
                val queryString = "%$query%"
                contactDao.findByNameLikeOrNumberLike(queryString, queryString).collect {
                    viewModel.contacts.value!!.clear()
                    viewModel.contacts.value!!.addAll(it)
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
            } else {
                contactDao.getContacts().collect {
                    viewModel.contacts.value!!.addAll(it)
                    // notify UI update
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            contactDao.getCount().collect {
                viewModel.contactPage.value!!.total = it
            }
        }
    }

    private fun initRecyclerView(root: View) {
        recyclerView = root.findViewById(R.id.contact_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =
            ContactsAdapter(viewModel.contacts.value!!,
                object : OnRecyclerReachBottomListener {
                    override fun onBottomReached(position: Int) {
                        if (viewModel.contactPage.value!!.total > viewModel.contactPage.value!!.offset) {
                            viewModel.contactPage.value!!.offset = position + 1
                            loadContactsFromDatabase()
                        }
                    }
                })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.contact_add -> {
                Log.i("OptionItem", "onOptionsItemSelected: addContact")
                val intent = Intent(context, AddContactActivity::class.java)
                startActivityForResult(intent, ActivityResult.ADD_CONTACT.code)
                true
            }

            R.id.contact_search -> {
                Log.i("OptionItem", "onOptionsItemSelected: searchContact")
//                val intent = Intent(context, SearchActivity::class.java)
//                startActivity(intent)
                true
            }

            R.id.settings1 -> {
                Log.i("OptionItem", "onOptionsItemSelected: moreOptions")
                findNavController().navigate(R.id.contact_navigate_to_search)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.contacts_menu, menu)
        // Associate searchable configuration with the SearchView
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.contact_search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        }
        val searchItem = menu.findItem(R.id.contact_search)
        searchItem.expandActionView()
        val searchView = searchItem.actionView
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            Log.e("TAG", "onActivityResult: some error")
            return
        }
        when (requestCode) {
            ActivityResult.ADD_CONTACT.code -> {
                // TODO: added Contact
            }
        }
    }

    private fun getContactList() {
        val cr: ContentResolver = requireContext().contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if ((cur?.count ?: 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                val id: String = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val avatar: String? = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts.PHOTO_URI)
                )
                val name: String = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                if (cur.getInt(
                        cur.getColumnIndex(
                            ContactsContract.Contacts.HAS_PHONE_NUMBER
                        )
                    ) > 0
                ) {
                    val pCur: Cursor? = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    if (pCur != null) {
                        while (pCur.moveToNext()) {
                            val phoneNo: String = pCur.getString(
                                pCur.getColumnIndex(
                                    ContactsContract.CommonDataKinds.Phone.NUMBER
                                )
                            )
                            Log.i("TAG", "Name: $name")
                            Log.i("TAG", "Phone Number: $phoneNo")
                            Log.i("TAG", "Phone URI: $avatar")
                        }
                    }
                    pCur?.close()
                }
            }
        }
        cur?.close()
    }

    fun getCallHistory() {
    }
}

enum class ActivityResult
    (
    val code: Int
) {
    ADD_CONTACT(0),
    SEARCH_CONTACT(1);
}

/**
 * Recycler Adapter, set recycler data and tab actions
 */
class ContactsAdapter(
    private val contacts: MutableList<Contact>,
    private val onRecyclerReachBottomListener: OnRecyclerReachBottomListener
) : RecyclerView.Adapter<ContactsAdapter.ViewHolder>() {


    /**
     * View Item holder, a recycler item
     */
    class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val contactItem: ConstraintLayout = view.findViewById(R.id.contact_card)
        private val nameTextView: TextView = view.findViewById(R.id.contact_card_username)
        private val avatarImageView: ImageView = view.findViewById(R.id.contact_card_avatar)
        private val messageView: ImageButton = view.findViewById(R.id.contact_item_message)
        private val callView: ImageButton = view.findViewById(R.id.contact_item_call)

        fun setData(contact: Contact) {
            setUpListener(contact)
            nameTextView.text = contact.username

            // animation
            val anim = ScaleAnimation(
                0.95f,
                1.0f,
                0.95f,
                1.0f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
            )

            anim.duration = 300
            contactItem.startAnimation(anim)
            if (contact.avatar == null) {
                avatarImageView.setImageResource(Consts.DEFAULT_AVATAR)
            } else {
                Glide.with(view).load(contact.avatar).into(avatarImageView)
            }
            callView.setOnClickListener {
                val uri = "tel:${contact.number}"
                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
//                intent.data = Uri.parse(uri)
                view.context.startActivity(intent)
            }

            messageView.setOnClickListener {
                val uri = "sms:${contact.number}"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                view.context.startActivity(intent)
            }
        }


        private fun setUpListener(contact: Contact) {
            contactItem.setOnClickListener {
                val actions =
                    ContactsFragmentDirections.actionNavigationContactsToNavigationDetails(
                        contactNumber = contact.number!!
                    )
                it.findNavController().navigate(actions)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.contact_list_item, parent, false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        if (position == contacts.size - 1) {
//            onRecyclerReachBottomListener.onBottomReached(position)
//        }
        val contact = contacts[position]
        holder.setData(contact)
    }

    override fun getItemCount(): Int {
        return contacts.size
    }
}