package com.example.treechat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_channel.*
import kotlinx.android.synthetic.main.activity_channel_list.*
import java.lang.Math.abs

class ChannelActivity : AppCompatActivity() {
    var channelname = ""
    var description: String=""
    private var members: MutableList<String> = ArrayList<String>()
    private var messages: MutableList<String> = ArrayList<String>()
    private lateinit var myAdapter1 : ArrayAdapter<String>
    private lateinit var myAdapter2 : ArrayAdapter<String>



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_channel)
        channelname = intent.getStringExtra("chan_name")
        Log.d("chan_name", channelname.toString())
        channeltitle2.text = channelname

        val fb = FirebaseDatabase.getInstance().reference
        Log.d("p2", "in db.reference")
        val channels7 = fb.child("channel")
        Log.d("p3", "in child channel: " + channels7.toString())
        val channeltree = channels7.orderByChild("name").equalTo(channelname)

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

        if (channelinfo.members != members){
            members = channelinfo.members
            setupMembers()
//            val size = channelinfo.members.size
//            val current_size = members.size
//            val len = abs(size - current_size)
//
//            for (i in 1..len) {
//                chann
//            }
        }

//        if (channelinfo.messages)
        members = channelinfo.members
        messages = channelinfo.messages
    }

    fun setupMessages() {
        myAdapter1 = ArrayAdapter(this, android.R.layout.simple_list_item_1, messages)
        channelchat.adapter = myAdapter1
        myAdapter1.notifyDataSetChanged()
    }

    fun setupMembers() {
        myAdapter2 = ArrayAdapter(this, android.R.layout.simple_list_item_1, members)
        memberlist.adapter = myAdapter2
        myAdapter2.notifyDataSetChanged()
    }
}
