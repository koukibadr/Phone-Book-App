package com.bkapp.phonebook.data.dao

import androidx.room.*
import com.bkapp.phonebook.data.model.Contact


@Dao
interface ContactDAO {

    @Insert
    fun insertAllContacts(contacts: List<Contact>)

    @Update
    fun updateContact(contact: Contact)

    @Delete
    fun deleteContact(contact: Contact)

    @Query("DELETE FROM contact")
    fun deleteAllContacts()

    @Query("SELECT * FROM contact")
    fun getAllContacts(): List<Contact>

    @Query("SELECT * from contact WHERE lastName LIKE :query OR name LIKE :query OR phoneNumber LIKE :query")
    fun filterContact(query: String): List<Contact>

}