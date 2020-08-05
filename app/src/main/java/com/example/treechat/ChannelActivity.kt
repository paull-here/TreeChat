package com.example.treechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_channel.*
import kotlinx.android.synthetic.main.activity_channel_list.*
import java.lang.Math.abs

class ChannelActivity : AppCompatActivity() {
    var channelname: String=""
    var description: String=""
    private var members: MutableList<String> = ArrayList<String>()
    private var messageIDs: MutableList<String> = ArrayList<String>()
    private var messagelist: MutableList<String> = ArrayList<String>()
    private var messagemap: HashMap<String, HashMap<String, String>> = HashMap<String, HashMap<String, String>>()
    private var typeindicator = object : GenericTypeIndicator<HashMap<String, HashMap<String, String>>>(){}
    private lateinit var myAdapter1 : ArrayAdapter<String>
    private lateinit var myAdapter2 : ArrayAdapter<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        channelname = intent.getStringExtra("chan_name")
        Log.d("chan_name", channelname.toString())
        channeltitle2.text = channelname

        // Retrieve data in channel node
        val fb = FirebaseDatabase.getInstance().reference
        Log.d("p2", "in db.reference")
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

        description = channelinfo.description
        members = channelinfo.members
        setupMembers()

        //TODO: retrieve data from message section in tree, not in the same place as channelinfo
        messageIDs = channelinfo.messages
        Log.d("messageids", messageIDs.toString())
        val fb = FirebaseDatabase.getInstance().reference
        val messagetree = fb.child("message").orderByKey()
        messagetree.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(data: DataSnapshot) {
                setupMessages(data)
                }

            override fun onCancelled(databaseError: DatabaseError) {
                // report/log the error
            }
        })
//        setupMessages()
    }

    fun setupMessages(data: DataSnapshot) {
        messagemap = data.getValue(typeindicator)!!
        Log.d("messagemap", messagemap.toString())
        for (ID in messageIDs) {
            Log.d("currID", ID.toString())
            val from = messagemap[ID]!!["from"]
            Log.d("from", from.toString())
            val text = messagemap[ID]!!["text"]
            Log.d("text", text.toString())
            val timestamp = messagemap[ID]!!["timestamp"]
            Log.d("timestamp", timestamp.toString())
            messagelist.add("$from: $text - $timestamp")


        }
        Log.d("messagelist", messagelist.toString())
        myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagelist)
        channelchat.adapter = myAdapter1
        myAdapter1.notifyDataSetChanged()
//        val fb = FirebaseDatabase.getInstance().reference
//        var typeindicator = object : GenericTypeIndicator<HashMap<String, HashMap<String, String>>>(){}
//        val messagetree = fb.child("message").orderByKey()
//        messagetree.addListenerForSingleValueEvent(object: ValueEventListener {
//            override fun onDataChange(data: DataSnapshot) {
//                messagemap = data.getValue(typeindicator)!!
//                Log.d("messagemap", messagemap.toString())
//                for (ID in messageIDs) {
//                    Log.d("currID", ID.toString())
//                    val from = messagemap[ID]!!["from"]
//                    Log.d("from", from.toString())
//                    val text = messagemap[ID]!!["text"]
//                    Log.d("text", text.toString())
//                    val timestamp = messagemap[ID]!!["timestamp"]
//                    Log.d("timestamp", timestamp.toString())
//                    messagelist.add("$from: $text - $timestamp")
//
//                    Log.d("messagelist", messagelist.toString())
//                    myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagelist)
//                    channelchat.adapter = myAdapter1
//                    myAdapter1.notifyDataSetChanged()
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {
//                // report/log the error
//            }
//        })
//        Log.d("messagelist", messagelist.toString())
//        myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messagelist)
//        channelchat.adapter = myAdapter1
//        myAdapter1.notifyDataSetChanged()
    }

    fun setupMembers() {
        myAdapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, members)
        memberlist.adapter = myAdapter2
        myAdapter2.notifyDataSetChanged()
    }
}
