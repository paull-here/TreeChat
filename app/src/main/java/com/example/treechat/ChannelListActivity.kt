package com.example.treechat

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.treechat.WelcomeActivity.Companion.autoLoginCheck
import com.example.treechat.WelcomeActivity.Companion.currentPassKey
import com.example.treechat.WelcomeActivity.Companion.currentUserKey
import com.example.treechat.WelcomeActivity.Companion.fb
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_channel_list.*
import kotlinx.coroutines.*


class ChannelListActivity() : AppCompatActivity() {

    lateinit var mAuth: FirebaseAuth
    private var channelToDelete = ""
    private var channelList: MutableList<String> = ArrayList()
    private lateinit var myAdapter: ArrayAdapter<String>
    private var typeindicator2 = object : GenericTypeIndicator<HashMap<String, String>>() {}
    private var current_user = ""
    private var creatingChannel = false
    private lateinit var channeltreelistener: ValueEventListener

    // Coroutines
    private var ChannelListActivityJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + ChannelListActivityJob)

    //Child listener
    val childEventListener = object : ChildEventListener {
        override fun onChildAdded(data: DataSnapshot, previousChildName: String?) {
            Log.d("PLca", "onChildAdded:" + data.key!!)

            uiScope.launch {
                toChannelListChange(data)
            }
        }

        override fun onChildChanged(data: DataSnapshot, previousChildName: String?) {
            Log.d("PLcc", "onChildChanged: ${data.key}")

            uiScope.launch {
                toChannelListChange(data)
            }
        }

        override fun onChildRemoved(data: DataSnapshot) {
            Log.d("PLcr", "onChildRemoved:" + data.key!!)

            uiScope.launch {
                toChannelListChange(data)
            }
        }

        override fun onChildMoved(dataSnapshot: DataSnapshot, previousChildName: String?) {
            Log.d("PLcm", "onChildMoved:" + dataSnapshot.key!!)
        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.w("PLclerror", "postComments:onCancelled", databaseError.toException())
            Toast.makeText(
                this@ChannelListActivity, "Failed to load comments.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel_list)

        val sharedPref = getSharedPreferences("test", MODE_PRIVATE) ?: return
        current_user = sharedPref.getString(currentUserKey, "default")!!

        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channelList)
        listofchannels.adapter = myAdapter

        fb.child("/channellist").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                uiScope.launch {
                    toChannelListChange(data)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        listofchannels.setOnItemLongClickListener { _, _, index, _ ->
            Log.d("Longclicklistener", "Registered item long click")
            channelToDelete = channelList[index]
            registerForContextMenu(listofchannels)
            false
        }

        listofchannels.setOnItemClickListener { context, _, index, _ ->
            //TODO: Figure out how to go from an onclicklistener by index to starting channel (DONE)
            //TODO: Fix bug where joining channel but channel not added to user's channels (DONE)
            Log.d("toChannel1", "Clicked channel from channel list, moving to ChannelActivity")

            // Commenting out to test if coroutine will uncouple the crashing stacked listeners
//            val myIntent = Intent(this, ChannelActivity::class.java)
//            myIntent.putExtra("chan_name", channelList[index])
//            startActivity(myIntent)
//            Log.d("toChannel2", "Clicked channel from channel list, moving to ChannelActivity")

            // TODO: Fix app crashing bug
            // Taking out as it was not in the old working code
            // The purpose of this code is to figure out if the user is in the channel's member list

            uiScope.launch {
                toChannelClick(index)
            }


        }
        fb.child("/channellist").addChildEventListener(childEventListener)

        //Testing out Child Listener, commenting out below
        // TODO: Fix bug where extra key/value pairs are being made for the same user (DONE)
        // TODO: Fix bug where channelnameslistener and channellistlistener keep triggering
        // Note: The channelnameslistener repeating was not the source of the issue, the app
        // functioned fine, it stopped repeating after 4 repeats
//        val channeltree = fb.child("/channel")
//        channeltreelistener = channeltree.addValueEventListener(object : ValueEventListener {
//            override fun onDataChange(data: DataSnapshot) {
//                uiScope.launch {
//                    toChannelListChange(data)
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // report/log the error
//                Log.d("p6", "Data didn't arrive")
//            }
//        })
    }

    suspend fun toChannelListChange(data: DataSnapshot) {
        withContext(Dispatchers.IO) {
            processChannelListData(data)

            withContext(Dispatchers.Main) {
                setupList()
            }
        }
    }

    fun processChannelListData(data: DataSnapshot) {
//        if (creatingChannel) {
//            return
//        } else if (this@ChannelListActivity.isFinishing) {
//            return
//        }
        // Comment out to test childeventlistener
//        Log.d("channellistlistener", data.key + ": " + data.value)
//        val channelnames = data.children.toMutableList()
//        var channels = ArrayList<String>()
//        for (channel in channelnames) {
//            channels.add(channel.key.toString())
//        }
//        Log.d("channelnameslistener", channels.toString())
//        channelList = channels

        Log.d("channellistlistener", data.key + ": " + data.value)
        val channelnames = data.children.toMutableList()
        var channels = ArrayList<String>()
        var channelsToDelete = ArrayList<String>()
        for (channel in channelnames) {
            channels.add(channel.key.toString())
        }
//        channelList = channels

        // If a channel was added
        Log.d("channelnameslistener", channels.toString())
        for (channel in channels) {
            if (channel !in channelList) {
                channelList.add(channel)
            }
        }
        Log.d("chanList add", channelList.toString())

        // If a channel was deleted
        for (channel in channelList) {
            if (channel !in channels) {
                channelsToDelete.add(channel)
            }
        }

        channelList.removeAll(channelsToDelete)

        Log.d("chanList removeall", channelList.toString())

    }

    fun setupList() {
//        if (this@ChannelListActivity.isFinishing) {
//            return
//        }
        Log.d("SetupList", "SetupChannelList")
//        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channelList)
//        listofchannels.adapter = myAdapter
//        myAdapter.notifyDataSetChanged()
        if (!(this@ChannelListActivity.isFinishing)) {
            myAdapter.notifyDataSetChanged()
        }
    }

    suspend fun toChannelClick(index: Int) {
        withContext(Dispatchers.IO) {
            channelClick(index)
        }
    }

    fun channelClick(index: Int) {
        fb.child("/channel/${channelList[index]}/members")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    var memberInChannel = false
                    for (key in data.children) {
                        if (key.value == current_user) {
                            memberInChannel = true
                        }
                    }
                    if (!memberInChannel) {
                        val memberkey = fb.child("/channel/${channelList[index]}/members")
                            .push().key.toString()
                        // Commenting out to test Childeventlistener
//                        fb.child("/channel/${channelList[index]}/members/$newMemberKey")
//                            .setValue(current_user)
//
//                        // TODO: implement adding channel registration to user properties in user node (DONE)
//                        fb.child("user/$current_user/channels/${channelList[index]}")
//                            .setValue(newMemberKey)

                        val updateChannel = HashMap<String, Any?>()
                        updateChannel["/channel/${channelList[index]}/members/$memberkey"] =
                            current_user

                        val updateUser = HashMap<String, Any?>()
                        updateUser["user/$current_user/channels/${channelList[index]}"] = memberkey

                        fb.updateChildren(updateUser)
                        fb.updateChildren(updateChannel)
                    }

                    val myIntent = Intent(applicationContext, ChannelActivity::class.java)
                    myIntent.putExtra("chan_name", channelList[index])
                    Log.d(
                        "toChannel2",
                        "Clicked channel from channel list, moving to ChannelActivity"
                    )
//        myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(myIntent)
//        finish()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
                }
            })

        // Commenting out to test if coroutine will uncouple the crashing stacked listeners
//        var memberInChannel = false
//        for (key in data.children) {
//            if (key.value == current_user) {
//                memberInChannel = true
//            }
//        }
//        if (!memberInChannel) {
//            val newMemberKey = fb.child("/channel/${channelList[index]}/members")
//                .push().key.toString()
//            fb.child("/channel/${channelList[index]}/members/$newMemberKey")
//                .setValue(current_user)
//
//            // TODO: implement adding channel registration to user properties in user node (DONE)
//            fb.child("user/$current_user/channels/${channelList[index]}")
//                .setValue(newMemberKey)
//        }
//
//        val myIntent = Intent(this@ChannelListActivity, ChannelActivity::class.java)
//        myIntent.putExtra("chan_name", channelList[index])
//        Log.d(
//        "toChannel2",
//        "Clicked channel from channel list, moving to ChannelActivity"
//        )
////        myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
//        startActivity(myIntent)
////        finish()
    }

    // TODO: implement adding channel registration to user properties in user node (DONE)
    // TODO: Bug where clicking on Add Channel with no text creates channels (DONE)
    fun onAddChannel(view: View) {
        uiScope.launch {
            addChannel()
        }


    }

    suspend fun addChannel() {
        withContext(Dispatchers.IO) {
            channelMatch()
        }
    }

    fun channelMatch() {
        val channelname = addChannelName.text.toString()
        Log.d("channelMatch() channame", channelname)
        if (channelname == "") {
            Toast.makeText(
                this,
                "Please enter a channel name.", Toast.LENGTH_SHORT
            ).show()
            return
        }
        val channelMatchTree = fb.child("/channel").equalTo(channelname)

        channelMatchTree.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // do something with data
                Log.d("p5", data.key + ": " + data.value)
                processChannelMatch(data, channelname)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })

        addChannelName.text.clear()
    }

    // TODO: Bug where creating a channel doesn't jump to next activity instead it goes back to list
    fun processChannelMatch(data: DataSnapshot, channelname: String) {
        if (!data.exists()) {
            creatingChannel = true

            val channel = Channel()
            channel.makeNewChannel(current_user, channelname)

            // Commenting out to test Channel data class function updateChildren
//            val memberkey = fb.child("/channel/$channelname/members").push().key.toString()
//            fb.child("/channel/$channelname/members/${memberkey}").setValue("$currentUser")
//            Log.d(
//                "memberkey-user",
//                fb.child("/channel/$channelname/members/${memberkey}").key.toString()
//            )
//            fb.child("/channel/$channelname/name").setValue(channelname)
//            Log.d("channel's name set", fb.child("/channel/$channelname/name").key.toString())

            // This did not work, same crashing bug
//            channel.updateUserChannel(currentUser, channelname, memberkey)

//            // TODO: Fix app crashing bug
//            // Taking out as it was not in the old working code
//            fb.child("user/$currentUser/channels/$channelname").setValue(memberkey)
//            Log.d("User's channel", "property updated")

//            channelList.add(channelname)
            // TODO: Fix app crashing bug
            // Taking out as it was not in the old working code
//            setupList()

            // TODO: Worth keeping this code here to show how to call startActivity with view
            //  context (DONE)
//            val curr_context = view.context
//            val curr_intent = Intent(curr_context, ChannelActivity::class.java)
//            curr_intent.putExtra("chan_name", channelname)
//            curr_context.startActivity(curr_intent)

            // Tried without this@ChannelListActivity
            val myIntent = Intent(applicationContext, ChannelActivity::class.java)
            Log.d("procMatch() channame", channelname)
            myIntent.putExtra("chan_name", channelname)
//            myIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(myIntent)

            // TODO: Fix app crashing bug
            // Taking out as it was not in the old working code
//            creatingChannel = false
//            finish()

        } else {
            Toast.makeText(
                this,
                "Channel already exists. $channelname", Toast.LENGTH_SHORT
            ).show()
            return
        }
    }

    override fun onCreateContextMenu(
        menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?
    ) {
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
    // TODO: Make it so that deleting a channel will remove it from each user node (DONE with helper)
    // TODO: Rewrite so that messages are retrieved using Message class
    fun deleteChannel() {
        Log.d("channeltodelete", channelToDelete)
        val messageKeysToDelete = ArrayList<String>()
        fb.child("/channel/$channelToDelete/messages")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(data: DataSnapshot) {
                    if (!data.exists()) {
                        deleteChannelHelper(channelToDelete)
                        Log.d(
                            "deletedchannel1",
                            "deletedchannel ${fb.child("/channel/$channelToDelete").key.toString()}"
                        )
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

                    deleteChannelHelper(channelToDelete)
                    var fb2 = FirebaseDatabase.getInstance().reference

                    Log.d(
                        "deletedchannel1",
                        "deletedchannel ${fb.child("/channel/$channelToDelete").key.toString()}"
                    )
                    Log.d(
                        "deletedchannellist",
                        "deletedchannel ${fb2.child("/channellist/$channelToDelete").key.toString()}"
                    )
                    fb.child("/channel/$channelToDelete").removeValue()

                    fb2.child("/channellist/$channelToDelete").removeValue()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d(
                                    "Success remove",
                                    "removechannellist success data: " + task.result
                                )
                            } else {
                                Log.d("Fail remove", "get failed with ", task.exception)
                            }
                        }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
                }
            })
    }

    fun deleteChannelHelper(channelname: String) {
        fb.child("user").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                for (user in data.children) {
                    val userChannelsMap = user.child("channels")
                    if (!userChannelsMap.exists()) {
                        continue
                    } else if (userChannelsMap.hasChild(channelname) &&
                        userChannelsMap.child(channelname).exists()
                    ) {
                        Log.d(
                            "removingchannelfromuser", "${user.key.toString()}: " +
                                    fb.child("user/${user.key}/channels/$channelname").key.toString()
                        )
                        // Uncomment when actually removing
                        fb.child("user/${user.key}/channels/$channelname").removeValue()
                    }
                }

            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        }

        )
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
        val sharedPref = getSharedPreferences("test", MODE_PRIVATE) ?: return
        val currentUser = sharedPref.getString(currentUserKey, "default")
        Log.d("accountToDelete", currentUserKey)
        val memberKeysToDelete = ArrayList<ArrayList<String>>()
        fb.child("/user/$currentUser/channels")
            .addListenerForSingleValueEvent(object : ValueEventListener {
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
                        Log.d(
                            "removingfromchannel",
                            fb.child("channel/${keyValuePair[0]}/members/${keyValuePair[1]}").key.toString()
                        )
                        // Uncomment when actually deleting
                        fb.child("channel/${keyValuePair[0]}/members/${keyValuePair[1]}")
                            .removeValue()
                        Log.d("deletedchannel", "deletedfrom: ${keyValuePair[0]}")
                    }

                    Log.d(
                        "deleteduser",
                        "deleteduser ${fb.child("/user/$currentUser").key.toString()}"
                    )
                    // Uncomment when actually deleting
                    fb.child("/user/$currentUser").removeValue()
                    logoutClick()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
                }
            })
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
        val sharedPref = getSharedPreferences("test", MODE_PRIVATE) ?: return
        with(sharedPref.edit()) {
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
//        finish()
    }

//    override fun onPause() {
//        super.onPause()
//        fb.child("channel").removeEventListener(channeltreelistener)
//        ChannelListActivityJob.cancel()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        fb.child("channel").removeEventListener(channeltreelistener)
//        ChannelListActivityJob.cancel()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        fb.child("channel").removeEventListener(channeltreelistener)
//        ChannelListActivityJob.cancel()
//    }
}


//    override fun onPause() {
//        super.onPause()
//        fb.child("channel").removeValueEventListener(channeltreelistener)
//        finish()
//    }
//
//    override fun onStop() {
//        super.onStop()
//        fb.child("channel").removeEventListener(channeltreelistener)
//        finish()
//    }
//}

