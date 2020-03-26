package com.example.myapp;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class HttpServerActivity extends Activity implements OnClickListener {

    public static final Camera CAMERA_INSTANCE = Camera.open();
    private TextView textView;
    private TextView threadCountTextView;
    private FrameLayout previewLayout;
    private Intent myService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        Button btn1 = findViewById(R.id.button1);
        Button btn2 = findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        this.textView = findViewById(R.id.textView);

        this.threadCountTextView = findViewById(R.id.editTextThreads);

        // Create our Preview view and set it as the content of our activity.
        CameraPreview mPreview = new CameraPreview(this, CAMERA_INSTANCE);
        previewLayout = findViewById(R.id.camera_preview);
        previewLayout.addView(mPreview);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.button1) {
            myService = new Intent(this, MyIntentService.class);
            final CharSequence text = this.threadCountTextView.getText();
            if (text != null && !text.toString().isEmpty()) {
                myService.putExtra("threadCnt", Integer.valueOf(text.toString()));
            } else {
                Log.d("Application", "Please specifiy number of threads used by server");
                return;
            }
            EventBus.getDefault().register(this);
            startService(myService);
        }
        if (v.getId() == R.id.button2) {
            EventBus.getDefault().unregister(this);
            stopService(myService);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Message message) {
        String data = message.getData().getString("request");
        data = data + "\n";
        textView.append(data);
    }
}
