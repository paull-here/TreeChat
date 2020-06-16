package com.example.treechat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
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
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.example.treechat.R // <----- changed
//import com.example.treechat.WelcomeActivity.Companion.FIREBASE_PASSWORD
//import com.example.treechat.WelcomeActivity.Companion.FIREBASE_USERNAME
import com.example.treechat.WelcomeActivity.Companion.currentPassKey
import com.example.treechat.WelcomeActivity.Companion.currentUserKey
//import androidx.core.app.ComponentActivity
//import androidx.core.app.ComponentActivity.ExtraData
//import androidx.core.content.ContextCompat.getSystemService
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
//import androidx.core.app.ActivityCompat.startActivityForResult
//import androidx.core.app.ComponentActivity
//import androidx.core.app.ComponentActivity.ExtraData
//import androidx.core.content.ContextCompat.getSystemService
//import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.GoogleAuthProvider
import java.nio.channels.Channel


class SignInActivity : AppCompatActivity() {

    private lateinit var mGoogleSignInClient: GoogleSignInClient
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var mAuth : FirebaseAuth
//    private lateinit var sharedPref: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        mAuth = FirebaseAuth.getInstance()
//        mAuth.signInWithEmailAndPassword(FIREBASE_USERNAME, FIREBASE_PASSWORD)

//        sharedPref = getPreferences(Context.MODE_PRIVATE)


        configureGoogleSignIn()
        setupUI()
//        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestEmail()
//            .build()
//
//        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun setupUI() {
        google_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        mAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                val myIntent = Intent(this, ChannelListActivity::class.java)
                myIntent.putExtra("id", 123)
                startActivity(myIntent)
            } else {
                Toast.makeText(this, "Google sign in failed :(", Toast.LENGTH_LONG).show()
            }
        }
    }

//    override fun onStart() {
//        super.onStart()
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            val myIntent = Intent(this, ChannelListActivity::class.java)
//            myIntent.putExtra("id", 123)
//            myIntent.putExtra("username", user)
//            startActivity(myIntent)
//        }
//    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
//        if (requestCode == RC_SIGN_IN) {
//            // The Task returned from this call is always completed, no need to attach
//            // a listener.
//            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
//            handleSignInResult(task)
//        }
//    }

//    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
//    try {
//        val account = completedTask.getResult(ApiException::class.java)
//
//        // Signed in successfully, show authenticated UI.
////        updateUI(account)
//    } catch (e: ApiException) {
//        // The ApiException status code indicates the detailed failure reason.
//        // Please refer to the GoogleSignInStatusCodes class reference for more information.
////        Log.w("pl5", "signInResult:failed code=" + e.getStatusCode());
////        updateUI(null);
//    }
//}

//    fun googleLogin(v: View) {
//        val signInIntent = mGoogleSignInClient.getSignInIntent()
//        startActivityForResult(signInIntent, RC_SIGN_IN)
//
//    }

    fun loginClick (view: View) {
        // Get person's user/pass
        val user = username.text.toString()
        val pass = password.text.toString()

        // Look up credentials in Firebase
        val fb = FirebaseDatabase.getInstance().reference
        val users = fb.child("user")
        val userstree = users.orderByChild("username").equalTo(user)
        Log.d("pl", userstree.toString())

        userstree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // do something with data
                processData(user, pass, data)
                Log.d("pl1", data.key + ": " + data.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("pl3", "Data didn't arrive")
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
        val username = chatuser.username
//        val hisPassword = data.child("password").value
        val password = chatuser.password
        if (pass == password) {
            Log.d("PL3", "Current CHECK IS:" + checkBox.isChecked.toString())
            if (checkBox.isChecked) {
//                val thread = Thread {
//                    val sharedPref = this.getPreferences(Context.MODE_PRIVATE)
//                    with (sharedPref.edit()) {
//                        putString("current_user", chatuser.username)
//                        putString("current_pass", chatuser.password)
//                        commit()
//                    }
//                }
//                thread.start()
                val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
                with (sharedPref.edit()) {
                    putString(currentUserKey, username)
                    putString(currentPassKey, password)
                    apply()
                }
                WelcomeActivity.username = username
                WelcomeActivity.password = password
                WelcomeActivity.signedInAndChecked = true
                val current_user = sharedPref.getString(currentUserKey, "default")
                val current_pass = sharedPref.getString(currentPassKey, "default")
                Log.d("PL2", "CURRENT USER IS: " + current_user.toString())
                Log.d("PL2", "CURRENT PASS IS: " + current_pass.toString())
            }

            val myIntent = Intent(this, ChannelListActivity::class.java)
            myIntent.putExtra("id", 123)
            myIntent.putExtra("username", user)
            startActivity(myIntent)

        } else {
            Toast.makeText(this,
                "Wrong! The password is $password", Toast.LENGTH_SHORT).show()
        }

    }


}
