package com.example.treechat

import android.content.Context
import android.util.Log
import com.google.firebase.database.Exclude
import com.example.treechat.WelcomeActivity.Companion.fb

// You need primary constructors with default values in order for data classes to work
data class Channel (
    var name: String ="",
//    var description: String="",
    var members: HashMap<String, String> = HashMap<String, String>(),
    var messages: HashMap<String, String> = HashMap<String, String>()
) {

    @Exclude
    fun toChannelMap(): Map<String, Any?> {
        return mapOf(
            "name" to name,
            "members" to members,
        )
    }

    fun makeNewChannel(currentUser: String, channelname: String) {
        val memberkey = fb.child("/channel/$channelname/members").push().key.toString()
        val channelkey = fb.child("/channel/$channelname/key").push().key.toString()

        val updateChannel = HashMap<String, Any?>()
        updateChannel["/channel/$channelname/key/$channelkey"] = channelname
        updateChannel["/channel/$channelname/members/$memberkey"] = currentUser
        updateChannel["/channel/$channelname/name"] = channelname
        updateChannel["/channellist/$channelname"] = channelkey
        fb.updateChildren(updateChannel).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Success UC", "UpdateChildren() success data: " + task.result)
            } else {
                Log.d("Fail Update", "get failed with ", task.exception)
            }
        }

        Thread.sleep(500)


        Log.d(
            "memberkey-user",
            fb.child("/channel/$channelname/members/${memberkey}").key.toString()
        )
        Log.d("channel's name set", fb.child("/channel/$channelname/name").key.toString())

    }
}