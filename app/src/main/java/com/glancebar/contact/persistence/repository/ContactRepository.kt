package com.glancebar.contact.persistence.repository

import android.os.AsyncTask
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact


/**
 *
 * @author Ethan Gary
 * @date 2020/12/27
 */
class ContactRepository(
    private val database: AppDatabase = AppDatabase.INSTANCE!!,
    private val contactDao: ContactDao = database.getContactDao()
) {

    fun insert(contact: Contact) {
        DataAsyncTask(contactDao).execute(contact)
    }

    companion object {
        class DataAsyncTask(val dao: ContactDao) : AsyncTask<Any, Void, Void>() {

            override fun doInBackground(vararg params: Any?): Void? {
                dao.insert(params[0] as Contact)
                return null
            }
        }
    }
}