package com.glancebar.contact.ui.contacts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.glancebar.contact.persistence.entity.Contact
import kotlinx.coroutines.launch

class ContactsViewModel : ViewModel() {

    private val _contacts =
        MutableLiveData<MutableList<Contact>>().apply {
            value = mutableListOf()
        }

    val contacts: LiveData<MutableList<Contact>> = _contacts

    init {
        viewModelScope.launch {
            // Coroutine that will be canceled when the ViewModel is cleared.
        }
    }

}