package com.example.treechat

data class Channel (
    var channelname: String ="",
    var description: String="",
    var members: ArrayList<String>,
    var messages: ArrayList<String>
)