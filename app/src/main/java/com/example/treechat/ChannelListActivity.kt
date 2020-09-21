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

    //Child listener, unused because buggy
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
            uiScope.launch {
                toChannelClick(index)
            }


        }

//        fb.child("/channellist").addChildEventListener(childEventListener)

        //Testing out Child Listener, commenting out below
        // TODO: Fix bug where extra key/value pairs are being made for the same user (DONE)
        // TODO: Fix bug where channelnameslistener and channellistlistener keep triggering
        // Note: The channelnameslistener repeating was not the source of the issue, the app
        // functioned fine, it stopped repeating after 4 repeats
        val channeltree = fb.child("/channellist")
        channeltreelistener = channeltree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                uiScope.launch {
                    toChannelListChange(data)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })
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
        Log.d("channellistlistener", data.key + ": " + data.value)
        Log.d("channelList", channelList.toString())
        val channelnames = data.children.toMutableList()
        var channels = ArrayList<String>()
        var channelsToDelete = ArrayList<String>()
        for (channel in channelnames) {
            Log.d("addChannel: ", channel.key.toString())
            channels.add(channel.key.toString())
        }
        channelList = channels
        Log.d("channelListend", channelList.toString())
    }

    fun setupList() {
        myAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, channelList)
        listofchannels.adapter = myAdapter
        myAdapter.notifyDataSetChanged()
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

                        // Needed to take out channel registration to user node as it was crashing
                        // the app
//                        // TODO: implement adding channel registration to user properties in user node (DONE)

                        val updateMember = HashMap<String, Any?>()
                        updateMember["/channel/${channelList[index]}/members/$memberkey"] =
                            current_user
//                        updateMember["user/$current_user/channels/${channelList[index]}"] = memberkey

                        fb.updateChildren(updateMember)
                    }

                    val myIntent = Intent(applicationContext, ChannelActivity::class.java)
                    myIntent.putExtra("chan_name", channelList[index])
                    Log.d(
                        "toChannel2",
                        "Clicked channel from channel list, moving to ChannelActivity"
                    )
                    startActivity(myIntent)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    // report/log the error
                }
            })

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

            val myIntent = Intent(applicationContext, ChannelActivity::class.java)
            Log.d("procMatch() channame", channelname)
            myIntent.putExtra("chan_name", channelname)
            startActivity(myIntent)

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
                        // Don't need this because decided to remove user node channels
//                        deleteChannelHelper(channelToDelete)
//                        Log.d(
//                            "deletedchannel1",
//                            "deletedchannel ${fb.child("/channel/$channelToDelete").key.toString()}"
//                        )

                        val deleteMap = HashMap<String, Any?>()
                        deleteMap.put("/channel/$channelToDelete", null)
                        deleteMap.put("/channellist/$channelToDelete", null)
                        fb.updateChildren(deleteMap)

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

                    val deleteMap = HashMap<String, Any?>()
                    deleteMap.put("/channel/$channelToDelete", null)
                    deleteMap.put("/channellist/$channelToDelete", null)
                    fb.updateChildren(deleteMap)
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
    }

    override fun onPause() {
        super.onPause()
        fb.child("channel").removeEventListener(channeltreelistener)
        ChannelListActivityJob.cancel()
    }

    override fun onStop() {
        super.onStop()
        fb.child("channel").removeEventListener(channeltreelistener)
        ChannelListActivityJob.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        fb.child("channel").removeEventListener(channeltreelistener)
        ChannelListActivityJob.cancel()
    }
}

