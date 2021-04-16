package com.example.simpledemo.listener;

import android.graphics.SurfaceTexture;

public interface CameraStatusListener {
    void onCameraIsReady(SurfaceTexture surfaceTexture, int width, int height);

    void onCameraNotReady();
}
