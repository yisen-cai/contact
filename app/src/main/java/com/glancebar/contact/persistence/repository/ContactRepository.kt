package com.glancebar.contact.persistence.repository

import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact


/**
 * A repository class handle data operations between data layer(dao/network) and data usage
 * @author Ethan Gary
 * @date 2020/12/23
 */
class ContactRepository(
    private val db: AppDatabase = AppDatabase.INSTANCE!!,
    private val contactDao: ContactDao = db.getContactDao()
) {
    suspend fun insert(contact: Contact) {
        contactDao.insert(contact)
    }
}