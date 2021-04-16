package com.example.simpledemo.manager;

import android.graphics.SurfaceTexture;
import android.media.Image;
import android.util.Rational;

import androidx.camera.core.CameraX;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageAnalysisConfig;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.PreviewConfig;
import androidx.lifecycle.LifecycleOwner;

import com.example.simpledemo.listener.CameraStatusListener;

import java.nio.ByteBuffer;

public class CameraManager {
    private static final String TAG = "CameraManager";

    private static CameraManager mInstance;

    public static CameraManager getInstance() {
        if (mInstance == null) {
            mInstance = new CameraManager();
        }

        return mInstance;
    }

    private CameraManager() {

    }

    public void openCamera(LifecycleOwner mLifecycleOwner, CameraStatusListener listener) {
        // 1. preview
        PreviewConfig previewConfig = new PreviewConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .setTargetAspectRatio(new Rational(3, 4))
                .build();

        Preview preview = new Preview(previewConfig);
        preview.setOnPreviewOutputUpdateListener(output -> {
            // 保存相机SurfaceTexture对象
            SurfaceTexture cameraSurfaceTexture = output.getSurfaceTexture();
            int outputWidth = output.getTextureSize().getWidth();
            int outputHeight = output.getTextureSize().getHeight();
            if (listener != null) {
                if (cameraSurfaceTexture != null) {
                    listener.onCameraIsReady(cameraSurfaceTexture, outputWidth, outputHeight);
                } else {
                    listener.onCameraNotReady();
                }
            }
        });

        // 2. ImageAnalysis
        ImageAnalysisConfig imageAnalysisConfig = new ImageAnalysisConfig.Builder()
                .setLensFacing(CameraX.LensFacing.BACK)
                .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
                .setTargetAspectRatio(new Rational(3, 4))
                .build();

        ImageAnalysis imageAnalysis = new ImageAnalysis(imageAnalysisConfig);
        imageAnalysis.setAnalyzer(new PhotoAnalyzer());

        CameraX.bindToLifecycle(mLifecycleOwner, preview, imageAnalysis);
    }

    private static class PhotoAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy imageProxy, int rotationDegrees) {
            final Image image = imageProxy.getImage();
            if (image == null) {
                return;
            }

            byte[] data = getYUV420FromImage(image);
            // Todo: 处理图片数据
        }

        private byte[] getYUV420FromImage(Image image) {
            Image.Plane[] planes = image.getPlanes();
            ByteBuffer yBuffer = planes[0].getBuffer();
            ByteBuffer uBuffer = planes[1].getBuffer();
            ByteBuffer vBuffer = planes[2].getBuffer();

            int ySize = yBuffer.remaining();
            int uSize = uBuffer.remaining();
            int vSize = vBuffer.remaining();

            byte[] yuv420 = new byte[ySize * 3 / 2];
            yBuffer.get(yuv420, 0, ySize);

            int stride1 = planes[1].getPixelStride();
            int stride2 = planes[2].getPixelStride();
            if (stride1 == 1 && stride2 == 1) {
                // YUV 420
                uBuffer.get(yuv420, ySize, uSize);
                vBuffer.get(yuv420, ySize + uSize, vSize);
            } else if (stride1 == 2 && stride2 == 2) {
                // YUV 422
                int start = (int) ySize * 5 / 4;
                for (int i = 0, j = 0; i < uSize; i++) {
                    if (i % 2 == 0) {
                        yuv420[ySize + j] = uBuffer.get(i);
                        yuv420[start + j] = vBuffer.get(i);
                        j++;
                    }
                }
            }

            return yuv420;
        }
    }
}
