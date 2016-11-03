package com.quemb.qmbform.view;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.lang.reflect.Field;
import java.util.logging.Logger;

import android.support.v4.app.ActivityCompat;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.quemb.qmbform.R;

import java.io.IOException;
import java.util.List;

/**
 * Created by saturnaa on 16-09-02.
 */
public class BarcodeScannerActivity extends Activity {

    private static final Logger LOGGER = Logger.getLogger("BarcodeScannerActivity");
    private static final int REQUEST_CAMERA_ACCESS = 1;
    private static final int CAMERA = CameraSource.CAMERA_FACING_BACK;
    private static final String PERMISSIONS_CAMERA = Manifest.permission.CAMERA;

    public static final String BARCODE_RESULT = "barcodeResult";

    private ProgressBar mProgressBar;
    private SurfaceView mSurfaceView;
    private TrackerView mTrackerView;
    private Switch mTorchToggle;

    private BarcodeDetector mBarcodeDetector;
    private CameraSource mCameraSource;
    private Camera mCamera;

    private Barcode mClosestBarcode;
    private Handler mHandler = new Handler();
    private  Thread mProgressIncrementThread;
    private Timer mTimer = new Timer();

    private int mScreenWidth;
    private int mScreenHeight;
    private int mScreenOrientation;
    private int mProgress = 0;

    private Object mProgressLock = new Object();
    private Object mSurfaceLock = new Object();

    private boolean mTimerActive = false;
    private boolean mSurfaceWasCreated = false;
    private boolean mInstantCaptureMode = false;

    private final Detector.Processor<Barcode> mBarcodeProcessor = new Detector.Processor<Barcode>() {
        @Override
        public void release() {
        }

        @Override
        public void receiveDetections(Detector.Detections<Barcode> detections) {
            final SparseArray<Barcode> barcodes = detections.getDetectedItems();

            if(mProgress < 100) {
                if(barcodes.size() > 0) {
                    Barcode closest = closestToTarget(barcodes);

                    if(mClosestBarcode == null && closest != null) {
                        mClosestBarcode = closest;
                        resetProgress(false);
                    } else if (closest == null) {
                        mClosestBarcode = null;
                        resetProgress(true);
                    } else if (!closest.rawValue.equals(mClosestBarcode.rawValue)) {
                        mClosestBarcode = closest;
                        resetProgress(false);
                    } else {
                        // Important for TrackerView tracking
                        mClosestBarcode = closest;
                    }
                } else {
                    mClosestBarcode = null;
                    resetProgress(true);
                }

                mTrackerView.setBarcode(mClosestBarcode);

                mHandler.post(new Runnable() {
                    public void run() {
                        mTrackerView.invalidate();
                    }
                });

            }
            if (mProgress >= 100 || (mInstantCaptureMode && mClosestBarcode != null)){
                String returnValue = mClosestBarcode.displayValue;

                Intent resultIntent = new Intent();
                resultIntent.putExtra(BARCODE_RESULT, returnValue);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.barcode_scanner_activity);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mInstantCaptureMode = sp.getBoolean("barcode_capture_mode", false);

        mProgressBar = (ProgressBar) findViewById(R.id.scanProgress);
        mTorchToggle = (Switch) findViewById(R.id.torchToggle);
        mTrackerView = (TrackerView) findViewById(R.id.trackerView);
        mSurfaceView = (SurfaceView) findViewById(R.id.cameraView);

        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                synchronized (mSurfaceLock) {
                    mSurfaceWasCreated = true;
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (mBarcodeDetector != null) mBarcodeDetector.release();
                if (mCamera != null) mCamera.release();
            }
        });

        if (mInstantCaptureMode) mProgressBar.setVisibility(View.GONE);

        mTorchToggle.setSaveEnabled(false);

        mTorchToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setTorchOn(mCameraSource, isChecked);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(PERMISSIONS_CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQUEST_CAMERA_ACCESS);
            } else {
                startInitiatingCameraSurface();
            }
        } else {
            startInitiatingCameraSurface();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        interruptProgressThread();
        if (mCameraSource != null) {
            mCameraSource.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        interruptProgressThread();

        if (mBarcodeDetector != null) {
            mBarcodeDetector.release();
        }

        if (mCameraSource != null) {
            mCameraSource.stop();
        }

        if (mCamera != null) {
            mCamera.release();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_ACCESS: {

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        LOGGER.log(Level.FINEST, "Granted");
                        startInitiatingCameraSurface();

                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        LOGGER.log(Level.SEVERE, "Permission Denied");
                    }
                }
                break;
            }
            default: {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    private void resetProgress(boolean stop) {
        synchronized (mProgressLock) {
            mTimerActive = !stop;
            mProgress = 0;
            mProgressBar.setProgress(mProgress);
        }
    }

    private Barcode closestToTarget(SparseArray<Barcode> barcodes) {
            Barcode closestBarcode = null;
            Barcode barcode;
            Point centerPoint;
            int distance;
            int shortestDistance = Integer.MAX_VALUE;

            int screenMiddleX = (int) Math.round(mScreenWidth * 0.5);
            int screenMiddleY = (int) Math.round(mScreenHeight * 0.5);

            Point crosshairPoint = new Point(screenMiddleX, screenMiddleY);

        for(int i = 0; i < barcodes.size(); i++) {
            barcode = barcodes.valueAt(i);
            centerPoint = findCenterPoint(barcode.cornerPoints);
            distance = distanceFrom(centerPoint, crosshairPoint);

            if (shortestDistance > distance) {
                closestBarcode = barcode;
                shortestDistance = distance;
            }

            if(shortestDistance > 150) {
                closestBarcode = mClosestBarcode;
            }
        }
        if(closestBarcode != null) {
            LOGGER.log(Level.SEVERE, closestBarcode.displayValue + " : " + shortestDistance);
        }
        return closestBarcode;
    }

    private Point findCenterPoint(Point[] cornerPoints) {

        int middleX = Math.round((cornerPoints[1].x - cornerPoints[0].x)/2 + cornerPoints[0].x);
        int middleY = Math.round((cornerPoints[3].y - cornerPoints[0].y)/2 + cornerPoints[0].y);

        return new Point(middleX, middleY);
    }

    private int distanceFrom(Point a, Point b) {
        return (int) Math.round(Math.sqrt(Math.pow((a.x - b.x), 2) + Math.pow((a.y - b.y), 2)));
    }

    private boolean setTorchOn(CameraSource cameraSource, boolean on) {
        if (mCamera != null) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(on ? Camera.Parameters.FLASH_MODE_TORCH :
                    Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);
            return true;
        }

        return false;
    }

    private void setCamera(CameraSource cameraSource) {
        Field[] declaredFields = CameraSource.class.getDeclaredFields();

        for (Field field : declaredFields) {
            if (field.getType() == Camera.class) {
                field.setAccessible(true);
                try {
                    Camera camera = (Camera) field.get(cameraSource);
                    if (camera != null) {
                        mCamera = camera;
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }

                break;
            }
        }
    }

    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    public boolean hasFlash(CameraSource cameraSource) {
        if (mCamera == null) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParameters();

        if (parameters.getFlashMode() == null) {
            return false;
        }

        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if (supportedFlashModes == null || supportedFlashModes.isEmpty() || supportedFlashModes.size() == 1 && supportedFlashModes.get(0).equals(Camera.Parameters.FLASH_MODE_OFF)) {
            return false;
        }

        return true;
    }

    private void startInitiatingCameraSurface() {
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (mSurfaceLock) {
                    if (mSurfaceWasCreated) {
                        initiateCameraSurface();
                        this.cancel();
                    }
                }
            }
        }, 0, 100);
    }

    private void initiateCameraSurface() {

        mBarcodeDetector = new BarcodeDetector.Builder(getApplicationContext()).setBarcodeFormats(
                Barcode.ALL_FORMATS).build();

        mCameraSource = new CameraSource.Builder(getApplicationContext(), mBarcodeDetector)
                .setRequestedPreviewSize(1600, 1200).setFacing(CAMERA)
                .setRequestedFps(30.0f).setAutoFocusEnabled(true).build();

        try {
            mBarcodeDetector.setProcessor(mBarcodeProcessor);
            mCameraSource.start(mSurfaceView.getHolder());
            setCamera(mCameraSource);

            setTorchOn(mCameraSource, mTorchToggle.isChecked());

            int width = mCameraSource.getPreviewSize().getWidth();
            int height = mCameraSource.getPreviewSize().getHeight();
            mScreenOrientation = getResources().getConfiguration().orientation;

            mScreenWidth = mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ?
                    width : height;
            mScreenHeight = mScreenOrientation == Configuration.ORIENTATION_LANDSCAPE ?
                    height : width;

            mTrackerView.setPreviewSize(mScreenWidth, mScreenHeight);

            if (!hasFlash(mCameraSource)) {
                mTorchToggle.setVisibility(View.GONE);
            }

            restartProgressThread();

        } catch(IOException ie) {
            LOGGER.log(Level.SEVERE, ie.getMessage());
        }
    }

    private Thread getProgressIncrementThread() {
        return  new Thread(new Runnable() {
            public void run() {
                mProgressBar.setProgress(0);

                while (mProgress < mProgressBar.getMax()) {

                    try {
                        Thread.sleep(15);
                    } catch (InterruptedException e) {
                        return;
                    }

                    synchronized (mProgressLock) {
                        if (mTimerActive) {
                            mProgress++;
                        } else {
                            mProgress = 0;
                        }
                    }

                    mHandler.post(new Runnable() {
                        public void run() {
                            synchronized (mProgressLock) {
                                mProgressBar.setProgress(mProgress);
                            }
                        }
                    });
                }
            }
        });
    }

    private void restartProgressThread() {
        if ((mProgressIncrementThread == null ||
                mProgressIncrementThread.getState() == Thread.State.NEW ||
                mProgressIncrementThread.getState() == Thread.State.TERMINATED) &&
                !mInstantCaptureMode) {

            mProgressIncrementThread = getProgressIncrementThread();
            mProgressIncrementThread.start();
        }
    }

    private void interruptProgressThread() {
        if(mProgressIncrementThread != null && !mInstantCaptureMode) {
            mProgressIncrementThread.interrupt();
        }
    }
}
