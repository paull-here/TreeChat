package com.example.treechat

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

// This will allow this class to be sorted according to one of it's attributes
class Message (val key: String, val from: String, val msgtext: String, val timestamp: ZonedDateTime)
    : Comparable<Message>
{
    @RequiresApi(Build.VERSION_CODES.O)
    private var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a zzz")

    @RequiresApi(Build.VERSION_CODES.O)
    override fun compareTo(other: Message): Int = this.timestamp.compareTo(other.timestamp)

    @RequiresApi(Build.VERSION_CODES.O)
    override fun toString(): String {
        return "$from: $msgtext - ${this.toTime()}"
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun toTime(): String {
        val formattedtime = timestamp.format(formatter)
        return formattedtime.toString()
    }

}