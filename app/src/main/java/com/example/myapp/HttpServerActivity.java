package com.example.myapp;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HttpServerActivity extends Activity implements OnClickListener {

    public static final Camera CAMERA_INSTANCE = Camera.open();
    private TextView textView;
    private final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message inputMessage) {
            String data = inputMessage.getData().getString("request");
            data = data + "\n";
            textView.append(data);
        }
    };
    private TextView threadCountTextView;
    private FrameLayout previewLayout;
    private Button takePictureBtn;
    private ThreadCountListener threadCountListener;
    private SocketServer s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        Button btn1 = findViewById(R.id.button1);
        Button btn2 = findViewById(R.id.button2);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        this.textView = findViewById(R.id.textView);
        this.takePictureBtn = findViewById(R.id.takePictureBtn);
        this.takePictureBtn.setOnClickListener(this);

        this.threadCountTextView = findViewById(R.id.editTextThreads);

        // Create our Preview view and set it as the content of our activity.
        CameraPreview mPreview = new CameraPreview(this, CAMERA_INSTANCE);
        previewLayout = findViewById(R.id.camera_preview);
        previewLayout.addView(mPreview);
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v.getId() == R.id.button1) {
            s = new SocketServer(handler);
            this.threadCountListener = new ThreadCountListener(s);
            this.threadCountTextView.addTextChangedListener(threadCountListener);
            final CharSequence text = this.threadCountTextView.getText();
            if (text != null && !text.toString().isEmpty()) {
                s.setThreadsAvailable(Integer.valueOf(text.toString()));
            }
            s.start();
            //s.scheduleTakePicture();
        }
        if (v.getId() == R.id.button2) {
            s.close();
            try {
                s.join();
                this.threadCountTextView.removeTextChangedListener(threadCountListener);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (v.getId() == R.id.takePictureBtn && s != null) {
            s.takePicture();
        }
    }

}
