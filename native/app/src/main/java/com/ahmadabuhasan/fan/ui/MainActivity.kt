package com.ahmadabuhasan.fan.ui

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ahmadabuhasan.fan.databinding.ActivityMainBinding
import com.ahmadabuhasan.fan.modal.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var dbRef: DatabaseReference

    private lateinit var adapter: ArrayAdapter<User>
    private var usersList = mutableListOf<User>()
    private var filterOptions = arrayOf("All", "Confirmed", "Unconfirmed")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database =
            FirebaseDatabase.getInstance("https://fanintek-bekasi-default-rtdb.asia-southeast1.firebasedatabase.app/")
        dbRef = database.reference.child("users")

        val uid = auth.currentUser?.uid
        if (uid != null) {
            getData()
        }

        spinner()
        adapter = ArrayAdapter(
            this@MainActivity,
            android.R.layout.simple_list_item_1
        )
        binding.listView.adapter = adapter
    }

    private fun getData() {
        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val userList = mutableListOf<User>()

                for (data in dataSnapshot.children) {
                    val user = data.getValue(User::class.java)
                    if (user != null) {
                        userList.add(user)
                    }
                }

                usersList = userList.toMutableList()
                adapter.clear()
                adapter.addAll(userList)
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@MainActivity,
                    "Failed to retrieve data.",
                    Toast.LENGTH_SHORT
                ).show()
            }

        })
    }

    private fun spinner() {
        binding.spinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)
        binding.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedOption = filterOptions[position]
                    filterData(selectedOption)
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }

    private fun filterData(selectedOption: String) {
        val filteredList = when (selectedOption) {
            "All" -> usersList
            "Confirmed" -> usersList.filter { it.confirmed }
            "Unconfirmed" -> usersList.filter { !it.confirmed }
            else -> usersList
        }

        adapter.clear()
        adapter.addAll(filteredList)
        adapter.notifyDataSetChanged()
    }

}