package com.example.treechat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.example.treechat.WelcomeActivity.Companion.autoLoginCheck
import com.google.firebase.database.*

class ChannelListActivity : AppCompatActivity() {

    lateinit var mAuth : FirebaseAuth
    private var channelToDelete = ""
    private var channelList: MutableList<String> = ArrayList()
    private lateinit var myAdapter : ArrayAdapter<String>
    private var fb = FirebaseDatabase.getInstance().reference
    private var typeindicator2 = object : GenericTypeIndicator<HashMap<String, String>>(){}


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        val current_user = sharedPref.getString(currentUserKey, "default")

        listofchannels.setOnItemLongClickListener{_,_,index,_ ->
            Log.d("Longclicklistener","Registered item long click")
            channelToDelete = channelList[index]
            registerForContextMenu(listofchannels)
            false
        }
        
        listofchannels.setOnItemClickListener{ _,_,index,_ ->
            //TODO: Figure out how to go from an onclicklistener by index to starting channel (DONE)
            Log.d("toChannel1", "Clicked channel from channel list, moving to ChannelActivity")
            fb.child("/channel/${channelList[index]}/members")
                .addListenerForSingleValueEvent(object: ValueEventListener {
                    override fun onDataChange(data: DataSnapshot) {
                        var memberInChannel = false
                        for (key in data.children) {
                            if (key.value == current_user) {
                                memberInChannel = true
                            }
                        }
                        if (!memberInChannel) {
                            val newMemberKey = fb.child("/channel/${channelList[index]}/members")
                                .push().key.toString()
                            fb.child("/channel/${channelList[index]}/members/$newMemberKey")
                                .setValue(current_user)

                            // TODO: implement adding channel registration to user properties in user node (DONE)
                            fb.child("user/$current_user/channels/${channelList[index]}")
                                .setValue(newMemberKey)
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

        // TODO: Fix bug where extra key/value pairs are being made for the same user (DONE)
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

    override fun onCreateContextMenu(
        menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        Log.d("createcontextmenu", "Reached onCreateContextMenu")
        menuInflater.inflate(R.menu.deletechannel, menu)
        Log.d("ChanneltoDelete", "Reached channel to delete= $channelToDelete")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.deleteoption -> {
                deleteChannel()
                return true
            }
            else -> {
                return super.onContextItemSelected(item)
            }
        }
    }

    // TODO: Fix bug where can't retrieve/delete messages from /message node (DONE)
    // This bug was because the program flow with listeners is not synchronous, have to execute
    // everything inside the listener if the rest of the code depends on it

    // TODO: Fix bug where channels with no messages can't be deleted (DONE)
    // Just needed to add a case where if the data doesn't exist, then execute this instead
    // TODO: Make it so that deleting a channel will remove it from each user node
    fun deleteChannel() {
        Log.d("channeltodelete", channelToDelete)
        val messageKeysToDelete = ArrayList<String>()
        fb.child("/channel/$channelToDelete/messages")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (!data.exists()) {
                        Log.d("deletedchannel1", "deletedchannel ${fb.child("/channel/$channelToDelete").key.toString()}")
                        fb.child("/channel/$channelToDelete").removeValue()
                        return
                    }
                    val messagemap = data.getValue(typeindicator2)!!
                    Log.d("messagemap7", messagemap.toString())
                    for (key in messagemap) {
                        messageKeysToDelete.add(key.key)
                    }
                    Log.d("messageKeysToDelete1", messageKeysToDelete.toString())

                    for (key in messageKeysToDelete) {
                        Log.d("deleting messages", fb.child("/message/$key").key.toString())
                        fb.child("message/$key").removeValue()
                        Log.d("deletedmessage1", "deletedmessage")
                    }

                    Log.d("deletedchannel1", "deletedchannel ${fb.child("/channel/$channelToDelete").key.toString()}")
                    fb.child("/channel/$channelToDelete").removeValue()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
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
            R.id.deleteAccountButton -> {
                deleteAccount()
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    // TODO: Implement deleting account, including removing user information from each channel (DONE)
    fun deleteAccount() {
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        val currentUser = sharedPref.getString(currentUserKey, "default")
        Log.d("accountToDelete", currentUserKey)
        val memberKeysToDelete = ArrayList<ArrayList<String>>()
        fb.child("/user/$currentUser/channels")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (!data.exists()) {
                        return
                    }
                    val channelsmap = data.getValue(typeindicator2)!!
                    Log.d("channelmap", channelsmap.toString())
                    for (key in channelsmap) {
                        var tempList = ArrayList<String>()
                        tempList.add(key.key)
                        tempList.add(key.value)
                        memberKeysToDelete.add(tempList)
                    }
                    Log.d("memberKeysToDelete", memberKeysToDelete.toString())

                    for (keyValuePair in memberKeysToDelete) {
                        Log.d("removingfromchannel", fb.child("channel/${keyValuePair[0]}/members/${keyValuePair[1]}").key.toString())
                        // Uncomment when actually deleting
                        fb.child("channel/${keyValuePair[0]}/members/${keyValuePair[1]}")
                            .removeValue()
                        Log.d("deletedchannel", "deletedfrom: ${keyValuePair[0]}")
                    }

                    Log.d("deleteduser", "deleteduser ${fb.child("/user/$currentUser").key.toString()}")
                    // Uncomment when actually deleting
                    fb.child("/user/$currentUser").removeValue()
                    logoutClick()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
                }
            })
    }

    // TODO: implement adding channel registration to user properties in user node (DONE)
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
            val currentUser = sharedPref.getString(currentUserKey, "default")
            val memberkey = fb.child("/channel/$channelname/members").push().key.toString()
            fb.child("/channel/$channelname/members/${memberkey}").setValue("$currentUser")
            fb.child("/channel/$channelname/name").setValue(channelname)
            fb.child("user/$currentUser/channels/$channelname")
                .setValue(memberkey)

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

    // TODO: Bug where you can't logout anymore, SharedPreferences cannot be read (DONE)
    // Initially thought:
    // Needed to put the persistencestateenabled above all other Firebase calls in onCreate in
    // WelcomeActivity, and it was crashing because SharedPreferences was open and being modified
    // when a new Intent was launched, due to shared preferences editing being asynchronous. Put the
    // Intent inside the sharedPref.edit(). Removed FirebaseAuth.getInstance().signOut()

    // Found out that:
    // SetPersistenceEnabled is causing all of these problems where it is making the SharedPref
    // crash, so created new class MyFirebaseApp that extends Application and setpersistencenabled
    // there instead and linked it in the manifest
    fun logoutClick() {
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE) ?: return
        with (sharedPref.edit()) {
            clear()
            remove(currentUserKey)
            remove(currentPassKey)
            remove(autoLoginCheck)
            val done = commit()
            Log.d("p15", "COMMIT IS: " + done.toString())
        }
        WelcomeActivity.username = ""
        WelcomeActivity.password = ""
        WelcomeActivity.signedInAndChecked = false

        Log.d("p12", "CURRENT USER IS: " + WelcomeActivity.username)
        Log.d("p12", "CURRENT PASS IS: " + WelcomeActivity.password)

        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this@ChannelListActivity, WelcomeActivity::class.java)
        startActivity(intent)
    }
}

