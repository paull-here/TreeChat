package com.example.treechat

// You need primary constructors with default values in order for data classes to work
data class Channel (
    var channelname: String ="",
    var description: String="",
    var members: ArrayList<String> = ArrayList<String>(),
    var messages: ArrayList<String> = ArrayList<String>()
)