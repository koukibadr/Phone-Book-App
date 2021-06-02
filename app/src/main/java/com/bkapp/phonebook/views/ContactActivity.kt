package com.bkapp.phonebook.views

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import com.bkapp.phonebook.data.model.Contact
import com.bkapp.phonebook.databinding.ActivityContactsBinding
import com.bkapp.phonebook.modules.list_contact.ListContactVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {

    private val GET_CONTACT_REQUEST = 101
    private val homeViewModel by viewModels<ListContactVM>()
    private lateinit var binding: ActivityContactsBinding
    private var permission: Int = 0
    private var contactList: MutableList<Contact> = mutableListOf()
    lateinit var contactListAdapter: ContactListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContactsBinding.inflate(layoutInflater)
        permission = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
        )
        initView()
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        checkContactsPermission()
    }

    private fun checkContactsPermission() {
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    GET_CONTACT_REQUEST
            )
        } else {
            displayContacts()
        }
    }

    private fun initView() {
        contactListAdapter = ContactListAdapter(contactList) {}
        binding.listContact.adapter = contactListAdapter
        binding.searchEditext.addTextChangedListener { editable ->
            homeViewModel.searchContact(editable.toString()).observe(this@ContactActivity, Observer { contacts ->
                this.contactList.clear()
                this.contactList.addAll(contacts)
                contactListAdapter.notifyDataSetChanged()
            })
        }
    }


    private fun displayContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            homeViewModel.fetchContacts(contentResolver)
        }
        homeViewModel.getAllContacts().observe(this@ContactActivity, Observer { contacts ->
            this.contactList.clear()
            this.contactList.addAll(contacts)
            contactListAdapter.notifyDataSetChanged()
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