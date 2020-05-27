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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_sign_in.*

class WelcomeActivity : AppCompatActivity() {
    companion object {
        const val currentUserKey = "current_user"
        const val currentPassKey = "current_pass"
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

        FirebaseApp.initializeApp(this)
        var mAuth = FirebaseAuth.getInstance()

        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        val current_user = sharedPref.getString(currentUserKey, "default")
        val current_pass = sharedPref.getString(currentPassKey, "default")
        Log.d("PL1", "CURRENT USER IS: " + current_user.toString())
        Log.d("PL1", "CURRENT PASS IS: " + current_pass.toString())
        if (current_user != "default" && current_user != null && current_pass != "default" &&
            current_pass != null) {
            autoLogin(current_user.toString(), current_pass.toString())
        }

        // ex. Mon Oct 28 12:46:11 EDT 2019
        val tie = java.util.Calendar.getInstance().time

        Log.d("pltime", tie.toString())
    }

//    override fun onPause() {
//        super.onPause()
//
//        val sharedPref = getPreferences(Context.MODE_PRIVATE)
//
//
//        val current_user = sharedPref.getString(currentUserKey, "default")
//        val current_pass = sharedPref.getString(currentPassKey, "default")
//        Log.d("PL7", "CURRENT USER IS: " + current_user.toString())
//        Log.d("PL7", "CURRENT PASS IS: " + current_pass.toString())
//    }
//
//    override fun onResume() {
//        super.onResume()
//        val sharedPref = getPreferences(Context.MODE_PRIVATE)
//        val current_user = sharedPref.getString(currentUserKey, "default")
//        val current_pass = sharedPref.getString(currentPassKey, "default")
//        Log.d("PL8", "CURRENT USER IS: " + current_user.toString())
//        Log.d("PL8", "CURRENT PASS IS: " + current_pass.toString())
//
//    }


    fun signIn (view: View) {
        val myIntent = Intent(this, SignInActivity::class.java)
        startActivity(myIntent)
    }

    fun autoLogin(user: String, pass: String) {
        // Get person's user/pass

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
