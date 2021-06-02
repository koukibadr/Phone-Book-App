package com.bkapp.phonebook.views

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bkapp.phonebook.R
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {
    private val RECORD_REQUEST_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        val permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    RECORD_REQUEST_CODE
            )
        } else {
            displayContacts()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RECORD_REQUEST_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "App can't read your phone contact", Toast.LENGTH_SHORT)
                            .show()
                } else {
                    displayContacts()
                }
            }
        }
    }

    private fun displayContacts() {
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
                                Toast.makeText(
                                        applicationContext,
                                        "me: $name, Phone No: $phoneNo",
                                        Toast.LENGTH_SHORT
                                ).show()
                            }
                            pCur.close()
                        }
                    }
                }
            }
            cur.close()
        }

    }
}