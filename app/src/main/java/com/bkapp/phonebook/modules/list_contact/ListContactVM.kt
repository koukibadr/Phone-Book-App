package com.bkapp.phonebook.modules.list_contact

import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.bkapp.phonebook.data.ContactDatabase
import com.bkapp.phonebook.data.model.Contact
import kotlinx.coroutines.Dispatchers

class ListContactVM @ViewModelInject constructor(
    private val contactDatabase: ContactDatabase
) : ViewModel() {

    fun displayContacts(contentResolver: ContentResolver) = liveData(Dispatchers.IO) {
        val contactList = mutableListOf<Contact>()
        val cr = contentResolver
        val cur: Cursor? = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if (cur != null) {
            if (cur.count > 0) {
                while (cur.moveToNext()) {
                    val id: String =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name: String =
                        cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                    if (cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)
                        ).toInt() > 0
                    ) {
                        val pCur: Cursor? = cr.query(
                            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id),
                            null
                        )
                        if (pCur != null) {
                            while (pCur.moveToNext()) {
                                val phoneNo: String =
                                    pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                val email: String =
                                    pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME))
                                contactList.add(
                                    Contact(
                                        name = name,
                                        lastName = "",
                                        mailAddress = email,
                                        phoneNumber = phoneNo,
                                        id = null
                                    )
                                )
                            }
                            pCur.close()
                        }
                    }
                }
            }
            cur.close()
        }
        emit(contactList)
    }

}