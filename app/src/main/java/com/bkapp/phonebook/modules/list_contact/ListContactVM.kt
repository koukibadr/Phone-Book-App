package com.bkapp.phonebook.modules.list_contact

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.bkapp.phonebook.data.ContactDatabase
import com.bkapp.phonebook.data.model.Contact
import com.bkapp.phonebook.views.ContactListAdapter
import kotlinx.coroutines.*

class ListContactVM @ViewModelInject constructor(
        private val contactDatabase: ContactDatabase,
) : ViewModel() {


    private var contactList: MutableList<Contact> = mutableListOf()
    var contactListAdapter: ContactListAdapter

    init {
        contactListAdapter = ContactListAdapter(contactList)
    }

    /**
     * fetch all contact from phone then save result list in database
     *
     * @param contentResolver: a ContentResolver instance that contain all information about device
     */
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


    /**
     * clear the database table and  save all contact list fetched in database
     * @param contacts: the contact list that will be saved
     */
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


    /**
     * return a livedata contain the flow of the contact list from the database
     *
     * @param lifecycleOwner: the application context to observe on livedata changes
     */
    fun getAllContacts(lifecycleOwner: LifecycleOwner) {
        contactDatabase.contactDao().getAllContacts().observe(lifecycleOwner, Observer { listResult ->
            this.contactList.clear()
            this.contactList.addAll(listResult)
            contactListAdapter.notifyDataSetChanged()
        })
    }


    /**
     * search a contact based on the query typed, the search is done on the contact name, lastname,
     * phone number
     *
     * @param query: the query typed in the search field
     * @param lifecycleOwner: the application context to observe on livedata changes
     */
    fun searchContact(lifecycleOwner: LifecycleOwner, query: String) {
        contactDatabase.contactDao().filterContact("%$query%").observe(lifecycleOwner, Observer { listResult ->
            this@ListContactVM.contactList.clear()
            this@ListContactVM.contactList.addAll(listResult)
            this@ListContactVM.contactListAdapter.notifyDataSetChanged()
        })
    }

}