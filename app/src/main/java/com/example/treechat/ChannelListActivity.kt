package com.example.treechat

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.treechat.WelcomeActivity.Companion.currentPassKey
import com.example.treechat.WelcomeActivity.Companion.currentUserKey
import com.example.treechat.WelcomeActivity.Companion.password
import com.example.treechat.WelcomeActivity.Companion.username
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_channel_list.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import com.example.treechat.R
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.database.FirebaseDatabase


class ChannelListActivity : AppCompatActivity() {

    lateinit var mAuth : FirebaseAuth
    private var channelList: MutableList<String> = ArrayList<String>()
    private lateinit var myAdapter : ArrayAdapter<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)
        channelList.add("test1")
        setupList()
//        setupUI()

        listofchannels.setOnItemClickListener{ _,_,index,_ ->
            //TODO: Figure out how to go from an onclicklistener by index to starting channel
            val myIntent = Intent(this, ChannelActivity::class.java)
            myIntent.putExtra("chan_name", channelList[index])
            startActivity(myIntent)
        }
    }

    fun setupList() {
        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channelList)
        listofchannels.adapter = myAdapter
        myAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.channelmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle presses on the action bar menu items
        when (item.itemId) {
            R.id.logoutButton -> {
                logoutClick()
                return true
            }
//            R.id.action_copy -> {
//                text_view.text = "Copy"
//                return true
//            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }
    fun addChannel (view: View) {
        val fb = FirebaseDatabase.getInstance().reference
        //TODO: ADD IN arraylist and adapter for channels
    }

//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//            case R.id.action_settings:
//            //Do something
//            ...
//            return true;
//            case R.id.action_edit:
//            //Do something else
//            ...
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    fun logoutClick() {
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            clear()
            remove(currentUserKey)
            remove(currentPassKey)
            val done = commit()
            Log.d("p15", "COMMIT IS: " + done.toString())

        }
        WelcomeActivity.username = ""
        WelcomeActivity.password = ""
        WelcomeActivity.signedInAndChecked = false

        Log.d("p12", "CURRENT USER IS: " + WelcomeActivity.username)
        Log.d("p12", "CURRENT PASS IS: " + WelcomeActivity.password)

        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, WelcomeActivity::class.java)
        startActivity(intent)
//        startActivity(WelcomeActivity.getLaunchIntent(this))
    }
}

//    override fun onPause() {
//        super.onPause()
//
//        val sharedPref = getPreferences(Context.MODE_PRIVATE)
//        if (WelcomeActivity.signedInAndChecked) {
//            with (sharedPref.edit()) {
//                putString(currentUserKey, WelcomeActivity.username)
//                putString(currentPassKey, WelcomeActivity.password)
//                apply()
//            }
//        }
//
//        val current_user = sharedPref.getString(currentUserKey, "default")
//        val current_pass = sharedPref.getString(currentPassKey, "default")
//        Log.d("p10", "CURRENT USER IS: " + current_user.toString())
//        Log.d("p10", "CURRENT PASS IS: " + current_pass.toString())
//    }

//    private fun setupUI() {
//        logout.setOnClickListener {
//            logoutClick()
//        }
//    }