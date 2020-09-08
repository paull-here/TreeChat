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

    fun createAccount(username: String, password: String, email: String, name: String) {
        val fbr = FirebaseDatabase.getInstance().reference
        val allusers = fbr.child("user")
        val newuser = allusers.child(username)
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
        newuser.child("email").setValue(email)
        newuser.child("name").setValue(name)
        newuser.child("username").setValue(username)
        newuser.child("password").setValue(password)

        if (checkBox.isChecked) {
            with(sharedPref.edit()) {
                putString(WelcomeActivity.currentUserKey, username)
                putString(WelcomeActivity.currentPassKey, password)
                putString(WelcomeActivity.autoLoginCheck, "true")
                apply()
            }
        } else {
            Log.d("PL25", "reached sharedPrefEdit")
            with(sharedPref.edit()) {
                putString(WelcomeActivity.currentUserKey, username)
                putString(WelcomeActivity.currentPassKey, password)
                putString(WelcomeActivity.autoLoginCheck, "false")
                apply()
                Log.d("PL26", "sharedPrefEdit done")
            }
            val current_user = sharedPref.getString(WelcomeActivity.currentUserKey, "default")
            val current_pass = sharedPref.getString(WelcomeActivity.currentPassKey, "default")
            Log.d("PL27", "CURRENT USER IS: " + current_user.toString())
            Log.d("PL27", "CURRENT PASS IS: " + current_pass.toString())
        }

        val myIntent = Intent(this, ChannelListActivity::class.java)
        startActivity(myIntent)
    }
}
