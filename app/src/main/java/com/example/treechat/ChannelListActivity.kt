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
import com.google.firebase.database.*

class ChannelListActivity : AppCompatActivity() {

    lateinit var mAuth : FirebaseAuth
    private var channelList: MutableList<String> = ArrayList()
    private lateinit var myAdapter : ArrayAdapter<String>
    private var fb = FirebaseDatabase.getInstance().reference
    private var typeindicator2 = object : GenericTypeIndicator<HashMap<String, String>>(){}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        val current_user = sharedPref.getString(currentUserKey, "default")

        listofchannels.setOnItemClickListener{ _,_,index,_ ->
            //TODO: Figure out how to go from an onclicklistener by index to starting channel
            Log.d("toChannel1", "Clicked channel from channel list, moving to ChannelActivity")
            var memberInChannelFlag = false
            fb.child("/channel/${channelList[index]}/members").equalTo(current_user)
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(data: DataSnapshot) {
//                        memberInChannelFlag = retrieveMemberMap(data, current_user!!)
//                        if (!memberInChannelFlag) {
//                            val newMemberKey = fb.child("/channel/${channelList[index]}/members")
//                                .push().key.toString()
//                            fb.child("/channel/${channelList[index]}/members/$newMemberKey")
//                                .setValue(current_user)
//                        }
                        if (!data.exists()) {
                            val newMemberKey = fb.child("/channel/${channelList[index]}/members")
                                .push().key.toString()
                            fb.child("/channel/${channelList[index]}/members/$newMemberKey")
                                .setValue(current_user)

                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                        // report/log the error
                    }
                })
            val myIntent = Intent(this, ChannelActivity::class.java)
            myIntent.putExtra("chan_name", channelList[index])
            startActivity(myIntent)
            Log.d("toChannel2", "Clicked channel from channel list, moving to ChannelActivity")
        }

        val channeltree = fb.child("/channel")
        channeltree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                processChannelListData(data)
                Log.d("channellistlistener", data.key + ": " + data.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })

    }
    fun processChannelListData(data: DataSnapshot) {
        val channelnames = data.children.toMutableList()
        var channels = ArrayList<String>()
        for (channel in channelnames) {
            channels.add(channel.key.toString())
        }
        Log.d("channelnameslistener", channels.toString())
        channelList = channels
        setupList()
    }

//    fun retrieveMemberMap(data: DataSnapshot, current_user: String) : Boolean {
//        var memberInChannelFlag = false
//        val memberMap = data.getValue(typeindicator2)!!
//        for (ID in memberMap) {
//            Log.d("membermapvalue", memberMap[ID.key].toString())
//            Log.d("current_user", current_user)
//            if (memberMap[ID.key] == current_user) {
//                memberInChannelFlag = true
//            }
//        }
//        return memberInChannelFlag
//    }

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
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    fun addChannel (view: View) {
        val channelname = addChannelName.text.toString()
        val channelMatchTree = fb.child("/channel").equalTo(channelname)

        channelMatchTree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // do something with data
                processChannelMatch(data, channelname)
                Log.d("p5", data.key + ": " + data.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })

        addChannelName.text.clear()
    }

    fun processChannelMatch (data: DataSnapshot, channelname: String) {
        if (!data.exists()) {
            val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
            val userID = sharedPref.getString(currentUserKey, "default")
            val memberkey = fb.child("/channel/$channelname/members").push()
            fb.child("/channel/$channelname/members/${memberkey.key}").setValue("$userID")
            fb.child("/channel/$channelname/messages")
            fb.child("/channel/$channelname/name").setValue(channelname)

            channelList.add(channelname)
            val myIntent = Intent(this, ChannelActivity::class.java)
            myIntent.putExtra("chan_name", channelname)
            startActivity(myIntent)
        } else {
            Toast.makeText(this,
                "Channel already exists. $channelname", Toast.LENGTH_SHORT).show()
            return
        }
    }

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
