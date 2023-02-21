package com.example.vlcproject;

import static org.opencv.imgproc.Imgproc.CHAIN_APPROX_SIMPLE;
import static org.opencv.imgproc.Imgproc.COLOR_RGBA2GRAY;
import static org.opencv.imgproc.Imgproc.RETR_TREE;
import static org.opencv.imgproc.Imgproc.THRESH_BINARY;
import static org.opencv.imgproc.Imgproc.contourArea;
import static org.opencv.imgproc.Imgproc.cvtColor;
import static org.opencv.imgproc.Imgproc.findContours;
import static org.opencv.imgproc.Imgproc.threshold;

import android.content.pm.PackageManager;
//import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

//import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends CameraActivity {

    CameraBridgeViewBase cameraBridgeViewBase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getPermission();

        cameraBridgeViewBase = findViewById(R.id.camera_view);

        cameraBridgeViewBase.setCvCameraViewListener(new CameraBridgeViewBase.CvCameraViewListener2() {
            private boolean isFlashlightOn = false;
            @Override
            public void onCameraViewStarted(int width, int height) {

            }

            @Override
            public void onCameraViewStopped() {

            }

            @Override
            public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
                // Convert the frame to grayscale
                Mat gray = new Mat();
                cvtColor(inputFrame.rgba(), gray, COLOR_RGBA2GRAY);

                // Apply a threshold to the grayscale image to create a binary image
                Mat binary = new Mat();
                threshold(gray, binary, 100, 255, THRESH_BINARY);

                // Find the contours in the binary image
                List<MatOfPoint> contours = new ArrayList<>();
                Mat hierarchy = new Mat();
                findContours(binary, contours, hierarchy, RETR_TREE, CHAIN_APPROX_SIMPLE);

                // Loop through the contours and find the one that represents the flashlight
                for (MatOfPoint contour : contours) {
                    double area = contourArea(contour);
                    if (area > 50000) { // Adjust this threshold based on flashlight beam size
                        isFlashlightOn = true;
                        break;
                    } else {
                        isFlashlightOn = false;
                    }
                }

                Log.d("FlashlightState", "Flashlight: " + isFlashlightOn);
                return inputFrame.rgba();
            }
        });

        if (OpenCVLoader.initDebug()) {
            System.out.println("OpenCV loaded successfully");
            cameraBridgeViewBase.enableView();
        } else {
            System.out.println("OpenCV not loaded");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.enableView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (cameraBridgeViewBase != null) {
            cameraBridgeViewBase.disableView();
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(cameraBridgeViewBase);

    }

    void getPermission() {
        if(checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, 3);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==3 && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                getPermission();
            }
        }
    }
}