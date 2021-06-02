package com.bkapp.phonebook.modules.list_contact

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bkapp.phonebook.data.ContactDatabase
import com.bkapp.phonebook.data.model.Contact
import kotlinx.coroutines.*

class ListContactVM @ViewModelInject constructor(
        private val contactDatabase: ContactDatabase
) : ViewModel() {

    suspend fun fetchContacts(contentResolver: ContentResolver) {
        val contactList = mutableListOf<Contact>()
        val contactCursor: Cursor? = contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null
        )
        if (contactCursor != null) {
            if (contactCursor.count > 0) {
                while (contactCursor.moveToNext()) {
                    val id: String =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String =
                            contactCursor.getString(contactCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    if (contactCursor.getString(
                                    contactCursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                            ).toInt() > 0
                    ) {
                        val contactItemsCursor: Cursor? = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id),
                                null
                        )
                        if (contactItemsCursor != null) {
                            while (contactItemsCursor.moveToNext()) {
                                val phoneNo: String = contactItemsCursor.getString(contactItemsCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                contactList.add(
                                        Contact(
                                                name = name,
                                                lastName = "",
                                                mailAddress = "",
                                                phoneNumber = phoneNo,
                                                id = null
                                        )
                                )
                            }
                            contactItemsCursor.close()
                        }
                    }
                }
            }
            contactCursor.close()
        }
        saveContactsToDatabase(contactList)
    }


    private suspend fun saveContactsToDatabase(contacts: List<Contact>) {
        val contactsDeletion = GlobalScope.async {
            withContext(Dispatchers.IO) {
                contactDatabase.contactDao().deleteAllContacts()
            }
        }
        contactsDeletion.await()
        CoroutineScope(Dispatchers.IO).launch {
            contactDatabase.contactDao().insertAllContacts(contacts)
        }
    }

    fun getAllContacts() = liveData(Dispatchers.IO) {
        emit(contactDatabase.contactDao().getAllContacts())
    }

}