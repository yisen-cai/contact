package com.glancebar.contact

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.glancebar.contact.databinding.ActivityAddContactBinding
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.repository.ContactRepository
import com.glancebar.contact.utils.notEmpty
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch


class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var contact: Contact
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()
    private val contactRepository = ContactRepository(AppDatabase.INSTANCE!!)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        initDataBinding()

        //新页面接收数据
        val bundle = this.intent.extras

        // ready to update
        if (bundle != null) {
            title = getString(R.string.edit_contact_des)
            val contactId = bundle.getLong(CONTACT_ID)
            loadContactData(contactId)
        }

        setLabelText()
    }

    private fun loadContactData(contactId: Long) {
        GlobalScope.launch {
            contactDao.getById(contactId).collect {
                contact.id = it.id
                contact.username = it.username
                contact.number = it.number
                contact.location = it.location
                contact.isMarked = it.isMarked
                contact.email = it.email
                contact.company = it.company
                contact.avatar = it.avatar
                contact.telephone = it.telephone
            }
        }
    }

    private fun initDataBinding() {
        contact = Contact()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact)
        binding.contact = contact
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save_contact_check -> {
                saveContact()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater.inflate(R.menu.add_contact_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    private fun saveContact() {
        // TODO: contact validation
        if (notEmpty(contact.number) && notEmpty(contact.username)) {
            if (contact.id == 0L) {
                contactRepository.insert(contact)
                Toast.makeText(
                    this,
                    getString(R.string.add_contact_success),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                contactRepository.update(contact)
                Toast.makeText(
                    this,
                    getString(R.string.update_contact_success),
                    Toast.LENGTH_SHORT
                ).show()
            }

            Toast.makeText(this, getString(R.string.added_contact), Toast.LENGTH_SHORT).show()
            val data = Intent()
            data.putExtra("msg", getString(R.string.add_contact_success))
            setResult(RESULT_OK, data)
            finish()
        } else {
            Toast.makeText(this, getString(R.string.name_and_number_cant_null), Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun setLabelText() {
        val nameTipTextView: TextView = findViewById(R.id.add_contact_name_tip)
        val nameTipText =
            "<font color=#000000>${nameTipTextView.text}</font><font color=#FF0000>*</font><font color=#000000>:</font>"
        nameTipTextView.text = Html.fromHtml(nameTipText, Html.FROM_HTML_MODE_LEGACY, null, null)

        val mobileTipTextView: TextView = findViewById(R.id.add_contact_number_tip)
        val mobileTipText =
            "<font color=#000000>${mobileTipTextView.text}</font><font color=#FF0000>*</font><font color=#000000>:</font>"
        mobileTipTextView.text =
            Html.fromHtml(mobileTipText, Html.FROM_HTML_MODE_LEGACY, null, null)
    }

    companion object {
        const val CONTACT_ID = "contact_id"
    }
}