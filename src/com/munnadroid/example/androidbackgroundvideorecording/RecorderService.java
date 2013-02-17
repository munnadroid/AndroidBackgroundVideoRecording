package com.munnadroid.example.androidbackgroundvideorecording;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class RecorderService extends Service {
	private static final String TAG = "RecorderService";
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private static Camera mServiceCamera;
	public static boolean mRecordingStatus;
	private MediaRecorder mMediaRecorder;
	private File directory;
	private int cameraType=0;
	

	@Override
	public void onCreate() {
		mRecordingStatus = false;
		mServiceCamera = MainActivity.mCamera;
		mSurfaceView = MainActivity.mSurfaceView;
		mSurfaceHolder = MainActivity.mSurfaceHolder;
		

		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		if (mRecordingStatus == false) {
		

			startRecording();
		}

		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopRecording();
		mServiceCamera.release();
		mRecordingStatus = false;

		super.onDestroy();
	}

	
	public boolean startRecording() {
		Log.v(TAG, "recording service starting..");
		try {
			mRecordingStatus = true;

			mServiceCamera = Camera.open(cameraType);
			mServiceCamera.setDisplayOrientation(90);
			Camera.Parameters params = mServiceCamera.getParameters();
			mServiceCamera.setParameters(params);
			Camera.Parameters p = mServiceCamera.getParameters();
			// p.set("orientation", "landscape");

			// final List<Size> listSize = p.getSupportedPreviewSizes();
			// Size mPreviewSize = listSize.get(2);
			// Log.v(TAG, "use: width = " + mPreviewSize.width
			// + " height = " + mPreviewSize.height);
			// p.setPreviewSize(320,240);
			// p.setPreviewFormat(PixelFormat.YCbCr_420_SP);

			mServiceCamera.setParameters(p);

			// try {
			// mServiceCamera.setPreviewDisplay(mSurfaceHolder);
			// mServiceCamera.startPreview();
			// }
			// catch (IOException e) {
			// Log.e(TAG, e.getMessage());
			// e.printStackTrace();
			// }

			mServiceCamera.unlock();

			mMediaRecorder = new MediaRecorder();
			mMediaRecorder.setCamera(mServiceCamera);
			mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
			mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.DEFAULT);
			mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
			mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
			mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

			directory = new File(Environment.getExternalStorageDirectory()
					.toString() + "/aa/");
			if (!directory.exists())
				directory.mkdirs();

			long currentTime = System.currentTimeMillis();

			String uniqueOutFile = Environment.getExternalStorageDirectory()
					.toString() + "/aa/videooutput" + currentTime + ".mp4";
			File outFile = new File(directory, uniqueOutFile);
			if (outFile.exists()) {
				outFile.delete();
			}

			mMediaRecorder.setOutputFile(uniqueOutFile);

			// mMediaRecorder.setVideoFrameRate(80);
			mMediaRecorder.setVideoSize(320, 240);
			mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

			mMediaRecorder.setOrientationHint(90);

			mMediaRecorder.prepare();

			mMediaRecorder.start();


			return true;
		} catch (IllegalStateException e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			Log.d(TAG, e.getMessage());
			e.printStackTrace();
			return false;
		}
	}
	
	

	public void stopRecording() {

		mMediaRecorder.stop();
		mMediaRecorder.release();
		Log.v(TAG, "recording service stopped");
	}
}
