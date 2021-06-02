package com.bkapp.phonebook.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bkapp.phonebook.databinding.ActivityContactsBinding
import com.bkapp.phonebook.modules.list_contact.ListContactVM
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {

    private val GET_CONTACT_REQUEST = 101
    private val homeViewModel by viewModels<ListContactVM>()
    private lateinit var binding: ActivityContactsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_CONTACTS
        )

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                GET_CONTACT_REQUEST
            )
        } else {
            displayContacts()
        }
        setContentView(binding.root)
    }

    private fun displayContacts() {
        homeViewModel.displayContacts(contentResolver).observe(this, Observer { contacts ->
            contacts.sortBy {
                it.name
            }
            binding.listContact.adapter = ContactListAdapter(contacts) {

            }
        })
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            GET_CONTACT_REQUEST -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "App can't read your phone contact", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    displayContacts()
                }
            }
        }
    }
}