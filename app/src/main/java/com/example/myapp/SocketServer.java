package com.example.myapp;

import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import static com.example.myapp.HttpServerActivity.CAMERA_INSTANCE;


public class SocketServer {

    private final Handler handler;
    private final int port = 11111;
    private Semaphore semaphore;
    private ServerSocket serverSocket;
    private boolean bRunning;
    private final MyPreviewCallback previewCallback;
    private final MyPictureCallback pictureCallback;

    public SocketServer(Handler handler, int threadCnt) {
        this.handler = handler;
        this.semaphore = new Semaphore(threadCnt);
        this.previewCallback = new MyPreviewCallback();
        this.pictureCallback = new MyPictureCallback();
        CAMERA_INSTANCE.setPreviewCallback(previewCallback);
    }

    public void scheduleTakePicture() {
        final Timer timerObj = new Timer();
        final TimerTask timerTaskObj = new TimerTask() {
            public void run() {
                CAMERA_INSTANCE.takePicture(null, null, pictureCallback);
            }
        };
        timerObj.schedule(timerTaskObj, 0, 5000);
    }

    public void close() {
        try {
            serverSocket.close();
            CAMERA_INSTANCE.setPreviewCallback(null);
        } catch (IOException e) {
            Log.d("SERVER", "Error, probably interrupted in accept(), see log");
            e.printStackTrace();
        }
        bRunning = false;
    }

    public void listen() {
        try {
            Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
                Log.d("SERVER", "Socket Waiting for connection");
                final Socket s = serverSocket.accept();
                Log.d("SERVER", "Socket Accepted");
                if (semaphore.tryAcquire()) {
                    final ClientThread clientThread = new ClientThread(s, handler, semaphore, previewCallback, pictureCallback);
                    clientThread.start();
                } else {
                    Log.d("SERVER", "All available threads are busy");
                }
            }
        } catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
                Log.d("SERVER", "Normal exit");
            else {
                Log.d("SERVER", "Error");
                e.printStackTrace();
            }
        } finally {
            serverSocket = null;
            bRunning = false;
        }
    }
}
