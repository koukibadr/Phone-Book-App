package com.bkapp.phonebook.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.bkapp.phonebook.data.dao.ContactDAO
import com.bkapp.phonebook.data.model.Contact

@Database(entities = [Contact::class], version = 2)
abstract class ContactDatabase : RoomDatabase() {

    abstract fun contactDao(): ContactDAO

    companion object {
        private var instance: ContactDatabase? = null

        @Synchronized
        fun getInstance(context: Context): ContactDatabase {
            if (instance == null)
                instance = Room.databaseBuilder(
                        context.applicationContext, ContactDatabase::class.java,
                        "contactDatabase"
                )
                        .fallbackToDestructiveMigration()
                        .build()

            return instance!!
        }
    }

}