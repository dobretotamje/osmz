package com.example.myapp;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

public class ThreadCountListener implements TextWatcher {

    private final SocketServer socketServer;

    public ThreadCountListener(SocketServer socketServer) {
        this.socketServer = socketServer;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.toString().isEmpty()) return;
        socketServer.setThreadsAvailable(Integer.valueOf(s.toString()));
        Log.d("Semafor", "Thread count changed to value: " + s);
    }
}
