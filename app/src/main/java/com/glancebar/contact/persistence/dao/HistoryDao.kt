package com.glancebar.contact.persistence.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.glancebar.contact.persistence.entity.History
import kotlinx.coroutines.flow.Flow


/**
 *
 * @author Ethan Gary
 * @date 2020/12/24
 */
@Dao
interface HistoryDao {

    @Query("select count(*) from t_history where number = :number and create_time = :createTime")
    fun findByNumberAndCreateTime(number: String, createTime: Long): Int

    @Query("select * from t_history order by create_time desc limit :offset, :size")
    fun findAllLimit(offset: Int, size: Int): Flow<List<History>>

    @Insert
    fun insert(history: History)

    @Insert
    fun insertAll(vararg history: History)

    @Query("delete  from t_history where number = :number")
    fun clearHistory(number: String)

    @Delete
    fun delete(history: History)
}
