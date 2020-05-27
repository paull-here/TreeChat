package com.example.treechat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_create_account.*
import kotlinx.android.synthetic.main.activity_create_account.checkBox
import kotlinx.android.synthetic.main.activity_sign_in.*

class CreateAccountActivity : AppCompatActivity() {

//    lateinit var mAuth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)

        val mAuth = FirebaseAuth.getInstance().getCurrentUser()
//        mAuth.signInWithEmailAndPassword(
//            FIREBASE_USERNAME,
//            FIREBASE_PASSWORD
//        )
    }

    fun processAccount(view: View) {
        val fbr = FirebaseDatabase.getInstance().reference
        val allusers = fbr.child("user")
        Log.d("pld", allusers.toString())
        val username = username2.text.toString()
        val password = password2.text.toString()
        val email = email.text.toString()
        val name = name.text.toString()
        Log.d("pld1", "" + username + " " + password+ " " + email + " " +  name)
        val match = allusers.orderByChild("username").equalTo(username)
        Log.d("pld2", match.toString())
        match.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                processMatch(data, username, password, email, name)
                Log.d("pld3", data.key + ": " + data.value)
                Log.d("pld4", data.toString())
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        })
    }

    fun processMatch(data: DataSnapshot, user: String, pass: String, email: String, name: String){
        if (data.value != null) {
            Toast.makeText(this,
                "User $user already exists. Please choose another username.",
                Toast.LENGTH_SHORT).show()
            return
        } else if (data.value == null){
            createAccount(user, pass, email, name)
        }
    }

    fun createAccount(user: String, pass: String, email: String, name: String) {
        val fbr = FirebaseDatabase.getInstance().reference
        val allusers = fbr.child("user")
        val newuser = allusers.child(user)
        newuser.child("email").setValue(email)
        newuser.child("name").setValue(name)
        newuser.child("username").setValue(user)
        newuser.child("password").setValue(pass)

        if (checkBox.isChecked) {
            val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
            with(sharedPref.edit()) {
                putString(WelcomeActivity.currentUserKey, user)
                putString(WelcomeActivity.currentPassKey, pass)
                apply()
            }
        }
        val myIntent = Intent(this, ChannelListActivity::class.java)
        startActivity(myIntent)
    }
}
