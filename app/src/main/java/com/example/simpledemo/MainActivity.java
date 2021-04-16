package com.example.simpledemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.view.TextureView;
import android.view.ViewGroup;

import com.example.simpledemo.listener.CameraStatusListener;
import com.example.simpledemo.manager.CameraManager;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextureView preview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        checkPermission();
    }

    private void checkPermission() {
        // 相机权限
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            List<String> permissionList = new ArrayList<>();
            permissionList.add(Manifest.permission.CAMERA);
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[0]), 1);
        } else {
            initCamera();
        }
    }

    private void initView() {
        preview = findViewById(R.id.preview);
    }

    private void initCamera() {
        CameraManager.getInstance().openCamera(this, new CameraStatusListener() {
            @Override
            public void onCameraIsReady(SurfaceTexture surfaceTexture, int width, int height) {
                ViewGroup parent = (ViewGroup) preview.getParent();
                parent.removeView(preview);
                parent.addView(preview);
                preview.setSurfaceTexture(surfaceTexture);
            }

            @Override
            public void onCameraNotReady() {

            }
        });
    }

    /**
     * 权限申请的回调结果
     *
     * @param requestCode  请求码
     * @param permissions  请求权限
     * @param grantResults 授权结果，是一个int型数组，若有多个授权，则依次读取
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initCamera();
            }
        }
    }
}