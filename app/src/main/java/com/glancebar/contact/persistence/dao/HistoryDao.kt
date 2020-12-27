package com.glancebar.contact.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import com.glancebar.contact.persistence.entity.History
import io.reactivex.Completable


/**
 *
 * @author Ethan Gary
 * @date 2020/12/24
 */
@Dao
interface HistoryDao {

    @Insert
    fun insert(history: History): Completable

    @Insert
    fun insertAll(vararg history: History): Completable

    @Delete
    fun delete(history: History): Completable
}
