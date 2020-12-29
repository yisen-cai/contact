package com.glancebar.contact.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.glancebar.contact.persistence.entity.History


/**
 *
 * @author Ethan Gary
 * @date 2020/12/24
 */
@Dao
interface HistoryDao {

    @Query("select count(*) from t_history where number = :number and create_time = :createTime")
    fun findByNumberAndCreateTime(number: String, createTime: Long): Int

    @Insert
    fun insert(history: History)

    @Insert
    fun insertAll(vararg history: History)

    @Delete
    fun delete(history: History)
}
