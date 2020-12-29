package com.glancebar.contact.persistence.repository

import android.os.AsyncTask
import com.glancebar.contact.persistence.dao.HistoryDao
import com.glancebar.contact.persistence.database.AppDatabase
import com.glancebar.contact.persistence.entity.History


/**
 *
 * @author Ethan Gary
 * @date 2020/12/29
 */
class HistoryRepository(
    private val database: AppDatabase = AppDatabase.INSTANCE!!,
    private val historyDao: HistoryDao = database.getHistoryDao()
) {

    fun insert(history: History) {
        DataAsyncTask(historyDao).execute(history)
    }

    companion object {
        class DataAsyncTask(val dao: HistoryDao) : AsyncTask<Any, Void, Any>() {
            override fun doInBackground(vararg params: Any?): Any? {
                when (params[1]) {
                    0 -> {
                        dao.insert(params[0] as History)
                    }
                    1 -> {

                    }
                }
                return null
            }
        }
    }
}