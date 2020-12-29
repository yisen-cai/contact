package com.glancebar.contact.persistence.dao

import androidx.room.*
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.ContactWithHistory
import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow

/**
 * https://developer.android.com/training/data-storage/room/async-queries
 */
@Dao
interface ContactDao {

    @Insert
    fun insert(contact: Contact)

    @Insert
    fun insertAll(vararg contact: Contact): Completable

    @Query("select * from t_contact where username like :username order by username")
    fun findByName(username: String): List<Contact>

    @Query("select * from t_contact where username like :name or number like :number order by username")
    fun findByNameLikeOrNumberLike(name: String, number: String): Flow<List<Contact>>

    @Query("DELETE FROM t_contact")
    fun deleteAll()

    @Query("select count(*) from t_contact")
    fun getCount(): Flow<Int>

    @Query("select * from t_contact where id = :contactId limit 1")
    fun getById(contactId: Long): Contact

    @Query("select * from t_contact where is_marked = 1")
    fun getFavorite(): Flow<List<Contact>>

    @Update(entity = Contact::class)
    fun update(contact: Contact): Completable

    @Query("update t_contact set is_marked = :value where id = :contactId")
    fun updateQuery(contactId: Long, value: Int): Int

    @Delete
    fun delete(contact: Contact): Completable

//    @Query("select * from word_table order by word asc")
//    fun getAllWords(): List<Word>

    /**
     * LiveData, which is a lifecycle library class for data observation, can help app respond
     * to data changes, If use a return value of LiveData in method description, Room generates
     * all necessary code to update the LiveData when the database is updated
     */
    @Query("select * from t_contact order by username asc")
    fun getAllContacts(): Flow<List<Contact>>


    @Query("select * from t_contact order by username asc")
    fun getContacts(): Flow<List<Contact>>

    @Query("select * from t_contact where number=:contactNumber order by username")
    fun getAllContactsAndHistory(contactNumber: String): Flow<ContactWithHistory?>
}