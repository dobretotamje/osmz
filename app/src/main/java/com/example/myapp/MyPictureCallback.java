package com.example.myapp;

import android.hardware.Camera;

public class MyPictureCallback implements Camera.PictureCallback {

    private byte[] previousTakenPicture;

    public byte[] getPreviousTakenPicture() {
        return previousTakenPicture;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if(data != null) previousTakenPicture = data;
        camera.startPreview();
    }
}
