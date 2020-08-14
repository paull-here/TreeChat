package com.example.treechat

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_channel.*
import kotlinx.android.synthetic.main.activity_channel_list.*
import java.lang.Math.abs
import java.lang.String.format
import java.text.MessageFormat.format
import java.time.*
import java.time.format.DateTimeFormatter

class ChannelActivity : AppCompatActivity() {
    var channelname: String=""
    var description: String=""
    private val fb = FirebaseDatabase.getInstance().reference
    private var members: MutableList<String> = ArrayList()
//    private var messageIDs: MutableList<String> = ArrayList()
    private var messagelist: MutableList<String> = ArrayList()
    private var messagemap: HashMap<String, HashMap<String, String>> = HashMap()
    private var typeindicator = object : GenericTypeIndicator<HashMap<String, HashMap<String, String>>>(){}
    @RequiresApi(Build.VERSION_CODES.O)
    private var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a zzz")
    private lateinit var myAdapter1 : ArrayAdapter<String>
    private lateinit var myAdapter2 : ArrayAdapter<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        channelname = intent.getStringExtra("chan_name")
        Log.d("chan_name", channelname.toString())
        channeltitle2.text = channelname

        // Retrieve data in channel node
//        val fb = FirebaseDatabase.getInstance().reference
//        Log.d("p2", "in db.reference")
        val listOfChannels = fb.child("channel")
        Log.d("p3", "in child channel: " + listOfChannels.toString())
        val channeltree = listOfChannels.orderByChild("name").equalTo(channelname)

        // This event listener is meant to keep listening to query or database reference it is
        // attached to
        channeltree.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                // do something with data
                processData(data, channelname)
                Log.d("p5", data.key + ": " + data.value)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
                Log.d("p6", "Data didn't arrive")
            }
        })
    }

    fun processData(arr: DataSnapshot, channelname: String) {
        if (!arr.hasChildren()) {
            Toast.makeText(this,
                "No such channel $channelname", Toast.LENGTH_SHORT).show()
            return
        }

        val data = arr.children.iterator().next()
        val channelinfo = data.getValue(Channel::class.java)!!
        // TODO: need to retrieve keys in order by using orderByKeys

        description = channelinfo.description
        val membersTree = fb.child("/channel/$channelname/members").orderByKey()
        membersTree.addListenerForSingleValueEvent( object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                val membersMap =
                    data.getValue(object : GenericTypeIndicator<HashMap<String, String>>() {})!!
                for (key in membersMap) {
                    members.add(key.value)
                }
                Log.d("members", members.toString())
                setupMembers()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        })
//        Log.d("membersTree", membersTree.toString())
//        val membervalues = channelinfo.members.values
//
//        for (value in membervalues) {
//            members.add(value)
//        }
//        Log.d("members", members.toString())
//        setupMembers()

        //TODO: retrieve data from message section in tree, not in the same place as channelinfo
//        val msgkeys = channelinfo.messages.keys
//        for (key in msgkeys) {
//            messageIDs.add(key)
//        }
//        messageIDs.sortedDescending()
//        // [-ME9H68vtAfbi31RHB2E, -ME9H54n-igo_KcjQ7JM, uniqueid1]
//        Log.d("messageids", messageIDs.toString())
//        setupMessages()

        val messageTree = fb.child("/channel/$channelname/messages").orderByKey()
        messageTree.addListenerForSingleValueEvent( object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                val messagesMap =
                    data.getValue(object : GenericTypeIndicator<HashMap<String, String>>() {})!!
//                for (key in messagesMap) {
//                    messageIDs.add(0, key.value)
//                }
//                Log.d("messageids", messageIDs.toString())
                setupMessages()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        })
    }

    fun setupMessages() {
//        val fb = FirebaseDatabase.getInstance().reference
        val messagetree = fb.child("message").orderByKey()
        messagetree.addListenerForSingleValueEvent(object: ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(data: DataSnapshot) {
                setupMessagesHelper(data)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        })
//        Log.d("messagelist", messagelist.toString())
//        myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagelist)
//        channelchat.adapter = myAdapter1
//        myAdapter1.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun setupMessagesHelper(data: DataSnapshot) {

        messagemap = data.getValue(typeindicator)!!
        val messagemapkeys = ArrayList<String>()
        val messagemapfroms = ArrayList<String>()
        val messagemapmsgtexts = ArrayList<String>()
        var messagemaptimestamps = ArrayList<String>()
        var messagemapZDTs = ArrayList<ZonedDateTime>()
        var listOfMsgObjs = ArrayList<Message>()

        // Populate message property arraylists with data
        for (key in messagemap) {
            val keys : String = key.key
            messagemapkeys.add(keys)
            val from : String = key.value["from"]!!
            messagemapfroms.add(from)
            val msgtext : String = key.value["text"]!!
            messagemapmsgtexts.add(msgtext)
            val rawTimestampZDT : String = key.value["rawTimestampZDT"]!!
            messagemaptimestamps.add(rawTimestampZDT)
//            val ZDT : String = key.value["ZDTstring"]!!
//            messagemapZDTs.add(ZDT)
        }
        Log.d("messagemapkeys", messagemapkeys.toString())
        Log.d("messagemapfroms", messagemapfroms.toString())
        Log.d("messagemapmsgtexts", messagemapmsgtexts.toString())
        Log.d("messagemaptimestamps", messagemaptimestamps.toString())

//         Convert timestamps to ZonedDateTime objects
        for (timestamp in messagemaptimestamps) {
            val ZDT = ZonedDateTime.parse(timestamp)
            messagemapZDTs.add(ZDT)
        }

        Log.d("messagemapZDTs", messagemapZDTs.toString())

        // Create Message objects and put in list to be sorted
        for (i in 0 until messagemapkeys.size) {
            val Messageobj = Message(messagemapkeys.get(i), messagemapfroms.get(i),
                messagemapmsgtexts.get(i), messagemapZDTs.get(i))
            listOfMsgObjs.add(Messageobj)
        }

        // .sort() sorts in-place, needs mutable list. .sorted() returns new list
        listOfMsgObjs.sort()

        for (msg in listOfMsgObjs) {
            val finalmessage = msg.toString()
            if (messagelist.contains(finalmessage)) {
                continue
            } else {
                messagelist.add(finalmessage)
            }
        }

//        Log.d("messagemap", messagemap.toString())
//        for (ID in messageIDs) {
//            Log.d("currID", ID.toString())
//            val from = messagemap[ID]!!["from"]
//            Log.d("from", from.toString())
//            val text = messagemap[ID]!!["text"]
//            Log.d("text", text.toString())
//            val timestamp = messagemap[ID]!!["timestamp"]
//            Log.d("timestamp", timestamp.toString())
//            val finalmessage = "$from: $text - $timestamp"
//            if (messagelist.contains(finalmessage)) {
//                continue
//            } else {
//                messagelist.add(finalmessage)
//            }
//        }
        Log.d("messagelist", messagelist.toString())
        myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagelist)
        channelchat.adapter = myAdapter1
        myAdapter1.notifyDataSetChanged()
    }

    fun setupMembers() {
        myAdapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, members)
        memberlist.adapter = myAdapter2
        myAdapter2.notifyDataSetChanged()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessageClick (view: View) {
        val sharedPref = getSharedPreferences("test", Context.MODE_PRIVATE)
        val from = sharedPref.getString(WelcomeActivity.currentUserKey, "default")
        Log.d("ca1-username", from)
        val msgtext = messagetext.text.toString()
        Log.d("ca2-msgtext", msgtext)
        var DateTimeZone = ZonedDateTime.now()
        val timestamp = DateTimeZone.format(formatter)
        Log.d("ca2-timestamp", timestamp)

        val channelmessagenode = fb.child("channel/$channelname/messages")
        val channelmessagekey = channelmessagenode.push().key.toString()
        channelmessagenode.child(channelmessagekey).setValue(channelmessagekey)

        val messagenode = fb.child("message").child(channelmessagekey)
        messagenode.child("from").setValue(from)
        messagenode.child("text").setValue(msgtext)
        messagenode.child("timestamp").setValue(timestamp)
        messagenode.child("rawTimestampZDT").setValue(DateTimeZone.toString())
        messagetext.text.clear()
    }

}
