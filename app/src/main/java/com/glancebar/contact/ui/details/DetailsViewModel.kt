package com.glancebar.contact.ui.details

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History

class DetailsViewModel : ViewModel() {
    var contact: LiveData<Contact>? = null
    var histories: LiveData<MutableList<History>> =
        MutableLiveData<MutableList<History>>().apply {
            value = mutableListOf()
        }
}