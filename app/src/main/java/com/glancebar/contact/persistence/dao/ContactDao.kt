package com.glancebar.contact.persistence.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.glancebar.contact.persistence.entity.Contact

@Dao
interface ContactDao {

    @Insert
    fun insert(contact: Contact)

    @Insert
    fun insertAll(vararg contact: Contact)

    @Query("select * from t_contact where username like :username order by username")
    fun findByName(username: String): List<Contact>

    @Query("DELETE FROM t_contact")
    fun deleteAll()

    @Delete
    fun delete(contact: Contact)

//    @Query("select * from word_table order by word asc")
//    fun getAllWords(): List<Word>

    /**
     * LiveData, which is a lifecycle library class for data observation, can help app respond
     * to data changes, If use a return value of LiveData in method description, Room generates
     * all necessary code to update the LiveData when the database is updated
     */
    @Query("select * from t_contact order by username asc")
    fun getAllWords(): LiveData<List<Contact>>
}