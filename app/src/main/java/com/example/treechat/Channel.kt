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

//    fun updateUserChannel(currentUser: String, channelname: String, memberkey: String) {
//
//        val userchannelmap = mapOf(channelname to memberkey)
//
//        val childUpdates = hashMapOf<String, Any>(
//            "/user/$currentUser/channels" to userchannelmap
//        )
//
//        fb.updateChildren(childUpdates)
//    }

    fun makeNewChannel(currentUser: String, channelname: String) {
        val memberkey = fb.child("/channel/$channelname/members").push().key.toString()
        val channelkey = fb.child("/channel/$channelname/key").push().key.toString()
        // Trying different way of updating children
//        val channelmembermap = mapOf(memberkey to currentUser)
////            val channelnamemap = mapOf("name" to channelname)
//        val channelmap = mapOf("members" to channelmembermap, "name" to channelname)
//        val userchannelmap = mapOf(channelname to memberkey)
//
//        val childUpdates = hashMapOf<String, Any>(
////                "/channel/$channelname/members" to channelmembermap,
//            "/channel/$channelname" to channelmap,
////                "/user/$currentUser/channels" to userchannelmap
//        )
//
//        fb.updateChildren(childUpdates)

        // Trying to update with channellist node now
//        val updateChannel = HashMap<String, Any?>()
//        updateChannel["/channel/$channelname/members/$memberkey"] = currentUser
//        updateChannel["/channel/$channelname/name"] = channelname

//        val updateUser = HashMap<String, Any?>()
//        updateUser["/user/$currentUser/channels/$channelname"] = memberkey
//
//        fb.updateChildren(updateUser)
//        fb.updateChildren(updateChannel)

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

        // TODO: Needed to take out channel registration to user node as it was crashing the app
        // Version omitting just the user node which stops the crashing
//        val updateUser = HashMap<String, Any?>()
//        updateUser["/user/$currentUser/channels/$channelname"] = memberkey
//        fb.updateChildren(updateUser)

        Log.d(
            "memberkey-user",
            fb.child("/channel/$channelname/members/${memberkey}").key.toString()
        )
        Log.d("channel's name set", fb.child("/channel/$channelname/name").key.toString())
        
//        // TODO: Fix app crashing bug
//        // Taking out as it was not in the old working code
//        fb.child("user/$currentUser/channels/$channelname").setValue(memberkey)
//        Log.d("User's channel", "property updated")

    }
}