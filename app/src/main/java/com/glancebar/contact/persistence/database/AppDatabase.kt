package com.glancebar.contact.persistence.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.glancebar.contact.persistence.dao.ContactDao
import com.glancebar.contact.persistence.dao.HistoryDao
import com.glancebar.contact.persistence.dao.TagDao
import com.glancebar.contact.persistence.entity.Contact
import com.glancebar.contact.persistence.entity.History
import com.glancebar.contact.persistence.entity.Tag

/**
 * Create database instance here
 * Version is used to a strategy for the database migration
 * @author Ethan Gary
 * @date 2020/12/23
 */
@Database(entities = [Contact::class, History::class, Tag::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    abstract fun getContactDao(): ContactDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getTagDao(): TagDao

    companion object {
        @Volatile
        var INSTANCE: AppDatabase? = null

        // Migration path definition from version 3 to version 4.
//        val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//
//            }
//        }

        fun initDatabase(context: Context) {
            // create database here
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sample-db"
            ).fallbackToDestructiveMigration()
//                .addMigrations(MIGRATION_1_2)
                .build()
        }
    }
}