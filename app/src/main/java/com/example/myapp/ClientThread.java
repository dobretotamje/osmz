package com.example.myapp;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.RequiresApi;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import static com.example.myapp.Request.parse;

public class ClientThread extends Thread {
    private final Socket socket;
    private final Handler handler;
    private final MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
    private final Semaphore semaphore;
    private final ClientSpecialUriHandler clientSpecialUriHandler;
    private File root = Environment.getExternalStorageDirectory();
    private String locPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    public ClientThread(Socket socket, Handler handler, Semaphore semaphore, MyPreviewCallback previewCallback, MyPictureCallback pictureCallback) {
        this.socket = socket;
        this.handler = handler;
        this.semaphore = semaphore;
        this.clientSpecialUriHandler = new ClientSpecialUriHandler(previewCallback, pictureCallback);
    }

    private void sendMessage(String stringMessage) {
        final Message message = Message.obtain(); //get null message
        final Bundle b = new Bundle();
        b.putString("request", stringMessage);
        message.setData(b);
        handler.sendMessage(message);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void run() {
        try (
                OutputStream o = socket.getOutputStream();
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            final Request request = parse(in);
            if (request == null) return;

            final String filepath = request.getPath();

            if (request.getType() == RequestType.GET) {

                if (clientSpecialUriHandler.handle(filepath, o, out)) return;

                File file = new File(locPath + "/" + filepath);
                if (file.exists() && file.isFile()) {
                    final String mimeType = mimeTypeMap.getMimeTypeFromExtension(filepath);
                    out.write("HTTP/1.1 200 Ok\n" +
                            "Content-Type: " + mimeType + "\n" +
                            "Content-length: " + file.length() + "\n" +
                            "\n");
                    out.flush();

                    IOUtils.copy(new FileInputStream(file), o);

                    sendMessage(request.toString() + " Length: " + file.length());

                } else if (file.exists() && file.isDirectory()) {
                    File otherFile = new File(root.getAbsolutePath() + "/" + filepath);
                    writeDir(out, request.toString(), otherFile);
                } else {
                    // unknown ? go to root folder
                    File otherFile = new File(root.getAbsolutePath() + "/");
                    writeDir(out, request.toString(), otherFile);
                }
            }

        } catch (IOException | InterruptedException e) {
            Log.d("Client", "Something failed", e);
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                Log.d("SERVER", "Socket Closed");
                semaphore.release();
            } catch (IOException e) {
                Log.d("Client", "Unable to close socket");
            }
        }
    }

    private void writeDir(BufferedWriter out, String request, File dirFile) throws IOException {
        String html = HTMLDirectoryListingBuilder.build(dirFile);

        out.write("HTTP/1.1 200 Ok\n" +
                "Content-Type: text/html\n" +
                "\n");

        sendMessage(request + " Length: " + html.length());

        out.write(html);
        out.flush();
    }
}
