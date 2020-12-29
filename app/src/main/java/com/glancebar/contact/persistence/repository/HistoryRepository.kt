package com.glancebar.contact.persistence.repository

import android.os.AsyncTask
import com.glancebar.contact.persistence.dao.ContactDao
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
    private val historyDao: HistoryDao = database.getHistoryDao(),
    private val contactDao: ContactDao = database.getContactDao()
) {

    fun insert(history: History):Int {
        return DataAsyncTask(historyDao, contactDao).execute(history, 0).get() as Int
    }

    companion object {
        class DataAsyncTask(val dao: HistoryDao, private val contactDao: ContactDao) :
            AsyncTask<Any, Void, Any>() {
            override fun doInBackground(vararg params: Any?): Any? {
                when (params[1]) {
                    0 -> {
                        val history = params[0] as History
                        val result =
                            dao.findByNumberAndCreateTime(history.number!!, history.createTime)
                        if (result == 0) {
                            val contactId = contactDao.getByNumber(history.number)
                            history.contactId = contactId
                            dao.insert(params[0] as History)
                            return 1
                        }
                        return 0
                    }
                    1 -> {

                    }
                }
                return null
            }
        }
    }
}