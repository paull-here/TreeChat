package com.example;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class TreeChatPersistence extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
//        FirebaseDatabase.getInstance().setPersistenceEnabled(false);

        //        val messageSync = FirebaseDatabase.getInstance().getReference("message")
//        messageSync.keepSynced(false)
//        val channelSync = FirebaseDatabase.getInstance().getReference("channel")
//        channelSync.keepSynced(false)
//        val userSync = FirebaseDatabase.getInstance().getReference("user")
//        userSync.keepSynced(false)
    }

}

