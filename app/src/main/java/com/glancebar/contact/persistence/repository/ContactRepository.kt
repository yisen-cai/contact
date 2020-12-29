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
        DataAsyncTask(contactDao).execute(contact, 1)
    }

    fun favorite(contact: Contact) {
        DataAsyncTask(contactDao).execute(contact, 2)
    }

    fun update(contact: Contact) {
        DataAsyncTask(contactDao).execute(contact, 5)
    }

    fun delete(contact: Contact) {
        DataAsyncTask(contactDao).execute(contact, 3)
    }

    fun getById(contactId: Long): Contact {
        return DataAsyncTask(contactDao).execute(contactId, 4).get() as Contact
    }

    companion object {
        class DataAsyncTask(val dao: ContactDao) : AsyncTask<Any, Void, Any>() {

            override fun doInBackground(vararg params: Any?): Any? {
                when (params[1]) {
                    1 -> {
                        dao.insert(params[0] as Contact)
                    }
                    2 -> {
                        val contact = params[0] as Contact
//                        dao.update(contact)
                        dao.updateQuery(contactId = contact.id, contact.isMarked)
                    }
                    3 -> {
                        dao.delete(params[0] as Contact)
                    }
                    4 -> {
                        return dao.getById(params[0] as Long)
                    }
                    5 -> {
                        dao.update(params[0] as Contact)
                    }
                }
                return null
            }
        }
    }
}