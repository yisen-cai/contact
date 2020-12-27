package com.glancebar.contact

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.glancebar.contact.databinding.ActivityAddContactBinding
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact


class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var contact: Contact
    private val contactDao = AppDatabase.INSTANCE!!.getContactDao()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        initDataBinding()

        setLabelText()
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
        contactDao.insert(contact)
        val data = Intent()
        data.putExtra("msg", getString(R.string.add_contact_success))
        setResult(RESULT_OK, data)
        finish()
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
}