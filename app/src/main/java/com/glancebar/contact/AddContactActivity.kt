package com.glancebar.contact

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.glancebar.contact.databinding.ActivityAddContactBinding
import com.glancebar.contact.persistence.entity.Contact


class AddContactActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContactBinding
    private lateinit var contact: Contact

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)
        contact = Contact()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact)
        binding.contact = contact

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


    fun saveContact(view: View) {
        val data = Intent()
        data.putExtra("streetkey", "streetname")
        data.putExtra("citykey", "cityname")
        data.putExtra("homekey", "homename")
        setResult(RESULT_OK, data)
        finish()
    }
}