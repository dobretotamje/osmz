package com.example.myapp;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MyPreviewCallback extends Thread implements Camera.PreviewCallback {

    private final List<DataOutputStream> clients = new ArrayList<>();

    public void addClient(OutputStream outputStream) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(outputStream);
        dataOutputStream.write(("HTTP/1.0 200 OK\r\n" +
                "Server: iRecon\r\n" +
                "Connection: close\r\n" +
                "Max-Age: 0\r\n" +
                "Expires: 0\r\n" +
                "Cache-Control: no-store, no-cache, must-revalidate, pre-check=0, post-check=0, max-age=0\r\n" +
                "Pragma: no-cache\r\n" +
                "Content-Type: multipart/x-mixed-replace; " +
                "boundary=" + "xyz" + "\r\n" +
                "\r\n").getBytes());
        dataOutputStream.flush();
        dataOutputStream.write(("--" + "xyz" + "\r\n").getBytes());

        clients.add(dataOutputStream);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        for (DataOutputStream client : clients) {
            try {
                client.write(("Content-type: image/jpeg\r\n" +
                        "Content-Length: " + data.length + "\r\n" +
                        "\r\n").getBytes());

                Camera.Parameters parameters = camera.getParameters();
                Camera.Size size = parameters.getPreviewSize();
                YuvImage image = new YuvImage(data, ImageFormat.NV21,
                        size.width, size.height, null);
                Rect rectangle = new Rect(0, 0, size.width, size.height);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                image.compressToJpeg(rectangle, 100, byteArrayOutputStream);

                byteArrayOutputStream.writeTo(client);

                client.write("\n".getBytes());
                client.write(("--" + "xyz" + "\r\n").getBytes());
                client.flush();
            } catch (IOException e) {
                e.printStackTrace();
                clients.remove(client);
            }
        }
    }
}
