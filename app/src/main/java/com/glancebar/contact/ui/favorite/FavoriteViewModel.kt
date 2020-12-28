package com.glancebar.contact.ui.favorite

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.glancebar.contact.persistence.entity.Contact

class FavoriteViewModel : ViewModel() {

    var contacts: LiveData<MutableList<Contact>> = MutableLiveData<MutableList<Contact>>().apply {
        value = mutableListOf()
    }
}