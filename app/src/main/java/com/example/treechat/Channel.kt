package com.example.treechat

// You need primary constructors with default values in order for data classes to work
data class Channel (
    var name: String ="",
//    var description: String="",
    var members: HashMap<String, String> = HashMap<String, String>(),
    var messages: HashMap<String, String> = HashMap<String, String>()
)