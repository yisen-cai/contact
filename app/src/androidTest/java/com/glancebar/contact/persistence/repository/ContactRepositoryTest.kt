package com.glancebar.contact.persistence.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.Contact
import org.hamcrest.CoreMatchers.equalTo
import org.junit.After
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ContactRepositoryTest {
    private lateinit var contactDao: ContactDao
    private lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        contactDao = db.getContactDao()
    }

    @Test
    fun insert() {
        val contact = Contact(
            avatar = "avatar",
            username = "username",
            number = "number"
        )
        contactDao.insert(contact)
        contact.id = 1
        val result = contactDao.findByName("username")
        assertThat(result[0], equalTo(contact))
    }

    fun getAll() {
        val results = contactDao.getAllContacts()
    }

    @After
    fun tearDown() {
        db.close()
    }
}