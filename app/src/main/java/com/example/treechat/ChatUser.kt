package com.example.treechat

data class ChatUser (
    var channels: HashMap<String, String> = HashMap<String, String>(),
    var email: String ="",
    var name: String = "",
    var password: String = "",
    var username: String = ""
)