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
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
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

    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()
    private val contactRepository = ContactRepository()
    private lateinit var recyclerView: RecyclerView
    private var query: String? = null
    private lateinit var contacts: MutableList<Contact>
    private lateinit var includeView: View
    private var importedCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        query = requireActivity().intent.getStringExtra(SearchManager.QUERY)

        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // TODO: difference
        contacts = mutableListOf()
        (requireActivity() as MainActivity).showNavigator()
        val root = inflater.inflate(R.layout.contacts_fragment, container, false)
        includeView = root.findViewById(R.id.is_empty_view)
        initRecyclerView(root)
        return root
    }


    private fun toggleIncludeView(contactsList: MutableList<Contact>) {
        if (contactsList.size == 0) {
            includeView.visibility = View.VISIBLE
        } else {
            includeView.visibility = View.GONE
        }
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
                    contacts.clear()
                    contacts.addAll(it)
                    toggleIncludeView(contacts)
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
            } else {
                contactDao.getContacts().collect {
                    contacts.clear()
                    contacts.addAll(it)
                    toggleIncludeView(contacts)
                    // notify UI update
                    recyclerView.adapter!!.notifyDataSetChanged()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            contactDao.getCount().collect {
            }
        }
    }

    private fun initRecyclerView(root: View) {
        recyclerView = root.findViewById(R.id.contact_recycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter =
            ContactsAdapter(contacts,
                object : OnRecyclerReachBottomListener {
                    override fun onBottomReached(position: Int) {
//                        if (o.total > viewModel.contactPage.value!!.offset) {
//                            viewModel.contactPage.value!!.offset = position + 1
//                            loadContactsFromDatabase()
//                        }
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
            R.id.import_contacts -> {
                getContactList()
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
                contacts.clear()
                loadContactsFromDatabase()
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

                            val result = contactRepository.insert(
                                Contact(
                                    username = name,
                                    number = phoneNo.replace(" ", "")
                                )
                            )
                            if (result > 0) {
                                importedCount += 1
                            }
                        }
                        loadContactsFromDatabase()
                        Toast.makeText(
                            requireContext(),
                            "${getString(R.string.imported_contacts)} $importedCount ${getString(R.string.unit)}",
                            Toast.LENGTH_SHORT
                        ).show()
                        importedCount = 0
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
//            callView.setOnClickListener {
//                val uri = "tel:${contact.number}"
//                val intent = Intent(Intent.ACTION_CALL, Uri.parse(uri))
////                intent.data = Uri.parse(uri)
//                view.context.startActivity(intent)
//            }
//
//            messageView.setOnClickListener {
//                val uri = "sms:${contact.number}"
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//                view.context.startActivity(intent)
//            }
        }


        private fun setUpListener(contact: Contact) {
//            contactItem.setOnClickListener {
//                val actions =
//                    ContactsFragmentDirections.actionNavigationContactsToNavigationDetails(
//                        contactId = contact.id
//                    )
//                it.findNavController().navigate(actions)
//            }
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