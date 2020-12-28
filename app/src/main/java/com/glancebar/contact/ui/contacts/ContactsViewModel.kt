package com.glancebar.contact.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.utils.Page
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val _contacts =
        MutableLiveData<MutableList<Contact>>().apply {
            value = mutableListOf()
        }

    val contacts: LiveData<MutableList<Contact>> = _contacts

    val contactPage: LiveData<Page> = MutableLiveData<Page>().apply {
        value = Page()
    }

    init {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
        }
    }

}