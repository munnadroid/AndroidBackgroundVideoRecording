package com.munnadroid.backgroundrecord;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class RecorderService extends Service implements SurfaceHolder.Callback {

    private static final String tag_ = RecorderService.class.getSimpleName();

    public static final String INTENT_VIDEO_PATH = "video_path";
    /**
     * It is used to check recording status
     */
    public static boolean mRecordingStatus;

    private WindowManager windowManager;
    private SurfaceView surfaceView;
    private Camera mServiceCamera = null;
    private MediaRecorder mMediaRecorder = null;
    private Context context;
    private File outFile;
    private String videpPath;

    /**
     * This override method is called when first instance of RecordServices is made and use to create layout for camera video recording.
     */
    @Override
    public void onCreate() {

        context = this;
        try {
            /** Create new SurfaceView, set its size to 1x1, move it to the top
             * left corner and set this service as a callback  */
            windowManager = (WindowManager) this
                    .getSystemService(Context.WINDOW_SERVICE);
            surfaceView = new SurfaceView(this);
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(1, 1,
                    WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                    PixelFormat.TRANSLUCENT);
            layoutParams.gravity = Gravity.LEFT | Gravity.TOP;
            windowManager.addView(surfaceView, layoutParams);
            surfaceView.getHolder().addCallback(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null)
            videpPath = intent.getStringExtra(INTENT_VIDEO_PATH);
        if (videpPath == null)
            videpPath = "/Video/";
        return super.onStartCommand(intent, flags, startId);
    }


    /**
     * Callback method which gets called when sufaceholder is create.
     * As surfaceHolder is created it initializes MediaRecorder and starts recording front camera video.
     *
     * @param surfaceHolder
     */
    @Override
    public void surfaceCreated(final SurfaceHolder surfaceHolder) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                setupSurface(surfaceHolder);
            }
        }).start();

    }

    /**
     * Initialize MediaRecorder to open and start recording front camera video
     *
     * @param surfaceHolder
     */
    private void setupSurface(SurfaceHolder surfaceHolder) {
        try {
            if (Build.VERSION.SDK_INT >= 18) {
                mServiceCamera = openFrontCameraNew();
            } else {
                mServiceCamera = openFrontFacingCamera();
            }

            if (mServiceCamera != null) {


                Camera.Parameters params = mServiceCamera.getParameters();

                if (Integer.parseInt(Build.VERSION.SDK) >= 8) {

                    Display display = ((WindowManager) context
                            .getSystemService(Context.WINDOW_SERVICE))
                            .getDefaultDisplay();

                    if (display.getRotation() == Surface.ROTATION_0) {
                        mServiceCamera.setDisplayOrientation(90);
                    } else if (display.getRotation() == Surface.ROTATION_270) {
                        mServiceCamera.setDisplayOrientation(0);
                    }
                } else {

                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        params.set("orientation", "portrait");
                        params.set("rotation", 90);
                    }
                    if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                        params.set("orientation", "landscape");
                        params.set("rotation", 0);
                    }
                }

                mServiceCamera.setParameters(params);

                mServiceCamera.unlock();

                mMediaRecorder = new MediaRecorder();
                mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

                    @Override
                    public void onInfo(MediaRecorder mr, int what, int extra) {
                        // TODO Auto-generated method stub

                    }
                });

                mMediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

                    @Override
                    public void onError(MediaRecorder mr, int what, int extra) {
                        // TODO Auto-generated method stub

                        mMediaRecorder.reset();
                        mMediaRecorder.release();
                        mMediaRecorder = null;

                        mServiceCamera.lock();
                        mServiceCamera.release();
                        mServiceCamera = null;

                        stopSelf();
                        return;

                    }
                });

                mMediaRecorder.setCamera(mServiceCamera);
                mMediaRecorder
                        .setAudioSource(MediaRecorder.AudioSource.DEFAULT);
                mMediaRecorder
                        .setVideoSource(MediaRecorder.VideoSource.DEFAULT);
                mMediaRecorder
                        .setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

                File directory = new File(Environment.getExternalStorageDirectory().getPath() + videpPath);
                if (!directory.exists())
                    directory.mkdirs();
                long currentTime = System.currentTimeMillis();
                String fileNameString = "videooutput" + currentTime + ".mp4";
                String uniqueOutFile = Environment.getExternalStorageDirectory().getPath() + videpPath
                        + fileNameString;
                outFile = new File(directory, uniqueOutFile);
                if (outFile.exists()) {
                    outFile.delete();
                }
                mMediaRecorder.setOutputFile(uniqueOutFile);
                mMediaRecorder.setVideoSize(480, 320);
                mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());

                mMediaRecorder.prepare();
                mRecordingStatus = true;
                try {
                    mMediaRecorder.start();
                } catch (Exception e) {
                    // TODO: handle exception
                    if (outFile.exists())
                        outFile.delete();

                }
            } else {
                Log.v(tag_,
                        "Camera is not available (in use or does not exist)");
                try {
                    if (outFile.exists())
                        outFile.delete();
                } catch (Exception e) {
                    return;
                }

                return;
            }

        } catch (IllegalStateException e) {

            Log.d(tag_, e.getMessage());
            e.printStackTrace();

        } catch (IOException e) {

            Log.d(tag_, e.getMessage());
            e.printStackTrace();
            if (outFile.exists())
                outFile.delete();

        }
    }

    // Stop recording and remove SurfaceView
    @Override
    public void onDestroy() {


        try {

            mRecordingStatus = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.reset();
                mMediaRecorder.release();
            }

            if (mServiceCamera != null) {
                mServiceCamera.lock();
                mServiceCamera.release();
                windowManager.removeView(surfaceView);
            }

        } catch (Exception e) {
        }


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        stopSelf();

    }

    @TargetApi(14)
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        stopSelf();

    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format,
                               int width, int height) {
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Open the camera.  First attempt to find and open the front-facing camera.
     *
     * @return a Camera object
     */
    private Camera openFrontFacingCamera() {


        // Look for front-facing mServiceCamera, using the Gingerbread API.
        // Java reflection is used for backwards compatibility with pre-Gingerbread APIs.
        try {
            Class<?> cameraClass = Class.forName("android.hardware.Camera");
            Object cameraInfo = null;
            Field field = null;
            int cameraCount = 0;
            Method getNumberOfCamerasMethod = cameraClass.getMethod("getNumberOfCameras");
            if (getNumberOfCamerasMethod != null) {
                cameraCount = (Integer) getNumberOfCamerasMethod.invoke(null, (Object[]) null);
            }
            Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
            if (cameraInfoClass != null) {
                cameraInfo = cameraInfoClass.newInstance();
            }
            if (cameraInfo != null) {
                field = cameraInfo.getClass().getField("facing");
            }
            Method getCameraInfoMethod = cameraClass.getMethod("getCameraInfo", Integer.TYPE, cameraInfoClass);
            if (getCameraInfoMethod != null && cameraInfoClass != null && field != null) {
                for (int camIdx = 0; camIdx < cameraCount; camIdx++) {
                    getCameraInfoMethod.invoke(null, camIdx, cameraInfo);
                    int facing = field.getInt(cameraInfo);
                    if (facing == 1) { // Camera.CameraInfo.CAMERA_FACING_FRONT
                        try {
                            Method cameraOpenMethod = cameraClass.getMethod("open", Integer.TYPE);
                            if (cameraOpenMethod != null) {
                                mServiceCamera = (Camera) cameraOpenMethod.invoke(null, camIdx);
                            }
                        } catch (RuntimeException e) {
                            Log.e(tag_, "Camera failed to open: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        // Ignore the bevy of checked exceptions the Java Reflection API throws - if it fails, who cares.
        catch (ClassNotFoundException e) {
            Log.e(tag_, "ClassNotFoundException" + e.getLocalizedMessage());
        } catch (NoSuchMethodException e) {
            Log.e(tag_, "NoSuchMethodException" + e.getLocalizedMessage());
        } catch (NoSuchFieldException e) {
            Log.e(tag_, "NoSuchFieldException" + e.getLocalizedMessage());
        } catch (IllegalAccessException e) {
            Log.e(tag_, "IllegalAccessException" + e.getLocalizedMessage());
        } catch (InvocationTargetException e) {
            Log.e(tag_, "InvocationTargetException" + e.getLocalizedMessage());
        } catch (InstantiationException e) {
            Log.e(tag_, "InstantiationException" + e.getLocalizedMessage());
        } catch (SecurityException e) {
            Log.e(tag_, "SecurityException" + e.getLocalizedMessage());
        }


        return mServiceCamera;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private Camera openFrontCameraNew() {
        Camera camera = null;
        boolean found = false;
        int i;
        for (i = 0; i < Camera.getNumberOfCameras(); i++) {
            Camera.CameraInfo newInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(i, newInfo);
            if (newInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                found = true;
                break;
            }
        }
        if (found) {
            camera = Camera.open(i);
        }
        return camera;
    }

}
