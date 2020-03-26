package com.example.myapp;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

public class MyIntentService extends IntentService {

    public final Handler messageHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            EventBus.getDefault().post(inputMessage);
        }
    };
    private SocketServer s;

    public MyIntentService() {
        super("MyIntentService");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        s.close();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final int threadCnt = intent.getIntExtra("threadCnt", 5);
        Log.d("Service", "Starting local server with " + threadCnt + " threads.");
        s = new SocketServer(messageHandler, threadCnt);
        s.scheduleTakePicture();
        s.listen();
    }
}
