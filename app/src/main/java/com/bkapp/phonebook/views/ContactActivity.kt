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
import com.bkapp.phonebook.databinding.ActivityContactsBinding
import com.bkapp.phonebook.modules.list_contact.ListContactVM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val GET_CONTACT_REQUEST = 101

@AndroidEntryPoint
class ContactActivity : AppCompatActivity() {

    private val listContactVM by viewModels<ListContactVM>()
    private lateinit var binding: ActivityContactsBinding
    private var permission: Int = 0

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


    /**
     * check if the permission [Manifest.permission.READ_CONTACTS] is granted or not
     * if it's not granted a request popup will be shown otherwise the app will extract the
     * contact lists
     */
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


    /**
     * initialize the screen components by setting the adapter to recycler view
     * and setting up the text changed listener on the search field
     */
    private fun initView() {
        binding.listContact.adapter =  listContactVM.contactListAdapter
        binding.searchEditext.addTextChangedListener { editable ->
            listContactVM.searchContact(this,editable.toString())
        }
    }


    /**
     * fetch all contacts from the content provider and display the result in the recycler view
     */
    private fun displayContacts() {
        CoroutineScope(Dispatchers.Main).launch {
            listContactVM.fetchContacts(contentResolver)
        }
        listContactVM.getAllContacts(this)
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