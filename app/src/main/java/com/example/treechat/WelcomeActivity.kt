package com.example.treechat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_sign_in.*

class WelcomeActivity : AppCompatActivity() {
    companion object {
        const val currentUserKey = "current_user"
        const val currentPassKey = "current_pass"
        const val autoLoginCheck = "autoLoginCheck"
        private lateinit var mDatabase: DatabaseReference
        var username = ""
        var password = ""
        var signedInAndChecked = false
        fun getLaunchIntent(from: Context) = Intent(from, WelcomeActivity::class.java)
            .apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        if (mDatabase == null) {
//            val database = FirebaseDatabase.getInstance()
//            database.setPersistenceEnabled(true)
//            mDatabase = database.reference
//        }
        FirebaseApp.initializeApp(this)
        val messageSync = FirebaseDatabase.getInstance().getReference("message")
        messageSync.keepSynced(true)
        val channelSync = FirebaseDatabase.getInstance().getReference("channel")
        channelSync.keepSynced(true)
        val userSync = FirebaseDatabase.getInstance().getReference("user")
        userSync.keepSynced(true)



        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        val current_user = sharedPref.getString(currentUserKey, "default")
        val current_pass = sharedPref.getString(currentPassKey, "default")
        Log.d("PL1", "CURRENT USER IS: " + current_user.toString())
        Log.d("PL1", "CURRENT PASS IS: " + current_pass.toString())
//        if (current_user != "default" && current_user != null && current_pass != "default" &&
//            current_pass != null) {
//            autoLogin(current_user.toString(), current_pass.toString())
//        }

        if (sharedPref.getString(autoLoginCheck, "default") == "true") {
            autoLogin(current_user.toString(), current_pass.toString())
        }

        // ex. Mon Oct 28 12:46:11 EDT 2019
        val tie = java.util.Calendar.getInstance().time

        Log.d("pltime", tie.toString())
    }

    fun signIn (view: View) {
        val myIntent = Intent(this, SignInActivity::class.java)
        startActivity(myIntent)
    }

    // TODO: Fix autologin and login staying as default in SharedPreferences
    fun autoLogin(user: String, pass: String) {
        // Get person's user/pass
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
        if (sharedPref.getString(autoLoginCheck, "default") != "true") {
            return
        }

        // Look up credentials in Firebase
        Log.d("pl", "in autoLogin")
        val fb = FirebaseDatabase.getInstance().reference
        Log.d("p2", "in db.reference")
        val users = fb.child("user")
        Log.d("p3", "in child user: " + users.toString())
        val userstree = users.orderByChild("username").equalTo(user)
        Log.d("p4", userstree.toString())

        userstree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // do something with data
                processData(user, pass, data)
                Log.d("p5", data.key + ": " + data.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })
    }


    fun processData(user: String, pass: String, arr: DataSnapshot){
        if (!arr.hasChildren()) {
            Toast.makeText(this,
                "No such user $user", Toast.LENGTH_SHORT).show()
            return
        }

        val data = arr.children.iterator().next()
        val chatuser = data.getValue(ChatUser::class.java)!!

        //        val hisName = data.child("name").value
        val hisName = chatuser.username
//        val hisPassword = data.child("password").value
        val hisPassword = chatuser.password
        if (pass == hisPassword) {

            val myIntent = Intent(this, ChannelListActivity::class.java)
            myIntent.putExtra("id", 123)
            myIntent.putExtra("username", user)
            startActivity(myIntent)

        } else {
            Toast.makeText(this,
                "Wrong! The password is $hisPassword", Toast.LENGTH_SHORT).show()
        }
    }

    fun createAccount(view: View) {
        val myIntent = Intent(this, CreateAccountActivity::class.java)
        startActivity(myIntent)
    }
}
