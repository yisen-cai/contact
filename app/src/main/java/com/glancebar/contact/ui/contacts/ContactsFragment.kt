package com.glancebar.contact.ui.contacts

import android.app.Activity.RESULT_OK
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.glancebar.contact.AddContactActivity
import com.glancebar.contact.R


class ContactsFragment : Fragment() {

    companion object {
        fun newInstance() = ContactsFragment()
    }

    private lateinit var viewModel: ContactsViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.contacts_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ContactsViewModel::class.java)
        // TODO: Use the ViewModel
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
                getContactList()
                true
            }

            R.id.settings1 -> {
                Log.i("OptionItem", "onOptionsItemSelected: moreOptions")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.contacts_menu, menu)
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
        val cr: ContentResolver = context!!.contentResolver
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