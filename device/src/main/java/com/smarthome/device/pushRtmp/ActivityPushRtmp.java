package com.smarthome.device.pushRtmp;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.smarthome.device.R;
import com.smarthome.librtmpsdk.ILiveVideoBroadcaster;
import com.smarthome.librtmpsdk.LiveVideoBroadcaster;
import com.smarthome.librtmpsdk.utils.Resolution;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.smarthome.device.utils.Constants.RTMP_URL;


public class ActivityPushRtmp extends AppCompatActivity {
    private Intent mPushServiceIntent;
//    private TextView mPushStatus;
    private Timer mTimer;
    private long mElapsedTime;
    public TimerHandler mTimerHandler;
//    private ImageButton mSettingsButton;

    private ViewGroup mRootView;
    boolean mIsRecording = false;

    private CameraResolutionsFragment mCameraResolutionsDialog;
    private GLSurfaceView mGLView;

//    private CameraSurfacevView mCameraSurfacevView;
    private ILiveVideoBroadcaster mLiveVideoBroadcaster;
    private Button mStartPushButton;

    private int mEnterSrc;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            LiveVideoBroadcaster.LocalBinder binder = (LiveVideoBroadcaster.LocalBinder) service;
            if (mLiveVideoBroadcaster == null) {
                mLiveVideoBroadcaster = binder.getService();
                mLiveVideoBroadcaster.init(ActivityPushRtmp.this, mGLView);

//                mLiveVideoBroadcaster.init(ActivityPushRtmp.this, mCameraSurfacevView);

                mLiveVideoBroadcaster.setAdaptiveStreaming(true);
            }
            mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);//CAMERA_FACING_FRONT

            if (mEnterSrc == 1) {
                toggleBroadcasting(getWindow().getDecorView());
                mEnterSrc = 0;
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mLiveVideoBroadcaster = null;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide title
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Intent intent = getIntent();
        mEnterSrc = intent.getIntExtra("source", 0);

        //binding on resume not to having leaked service connection
        mPushServiceIntent = new Intent(this, LiveVideoBroadcaster.class);
        //this makes service do its job until done
        startService(mPushServiceIntent);

        setContentView(R.layout.activity_live_video_broadcaster);

        mTimerHandler = new TimerHandler();
//        mStreamNameEditText = (EditText) findViewById(R.id.stream_name_edit_text);

        mRootView = (ViewGroup)findViewById(R.id.root_layout);
//        mSettingsButton = (ImageButton)findViewById(R.id.settings_button);
//        mPushStatus = (TextView) findViewById(R.id.stream_live_status);

        mStartPushButton = (Button) findViewById(R.id.start_push);

        // Configure the GLSurfaceView.  This will start the Renderer thread, with an
        // appropriate EGL activity.
        mGLView = (GLSurfaceView) findViewById(R.id.cameraPreview_surfaceView);
        if (mGLView != null) {
            mGLView.setEGLContextClientVersion(2);     // select GLES 2.0
        }

//        mCameraSurfacevView = (CameraSurfacevView) findViewById(R.id.cameraPreview_surfaceView);

    }

    public void changeCamera(View v) {
//        if (mLiveVideoBroadcaster != null) {
//            mLiveVideoBroadcaster.changeCamera();
//        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //this lets activity bind
        bindService(mPushServiceIntent, mConnection, 0);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LiveVideoBroadcaster.PERMISSIONS_REQUEST: {
                if (mLiveVideoBroadcaster.isPermissionGranted()) {
                    mLiveVideoBroadcaster.openCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                }
                else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.CAMERA) ||
                            ActivityCompat.shouldShowRequestPermissionRationale(this,
                                    Manifest.permission.RECORD_AUDIO) ) {
                        mLiveVideoBroadcaster.requestPermission();
                    }
                    else {
                        new AlertDialog.Builder(ActivityPushRtmp.this)
                                .setTitle(R.string.permission)
                                .setMessage(getString(R.string.app_doesnot_work_without_permissions))
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {

                                        try {
                                            //Open the specific App Info page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));
                                            startActivity(intent);

                                        } catch ( ActivityNotFoundException e ) {
                                            //e.printStackTrace();

                                            //Open the generic Apps page:
                                            Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
                                            startActivity(intent);

                                        }
                                    }
                                })
                                .show();
                    }
                }
                return;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //hide dialog if visible not to create leaked window exception
        if (mCameraResolutionsDialog != null && mCameraResolutionsDialog.isVisible()) {
            mCameraResolutionsDialog.dismiss();
        }
        mLiveVideoBroadcaster.pause();
    }


    @Override
    protected void onStop() {
        super.onStop();
        unbindService(mConnection);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE || newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLiveVideoBroadcaster.setDisplayOrientation();
        }

    }

    public void showSetResolutionDialog(View v) {

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment fragmentDialog = getSupportFragmentManager().findFragmentByTag("dialog");
        if (fragmentDialog != null) {

            ft.remove(fragmentDialog);
        }

        ArrayList<Resolution> sizeList = mLiveVideoBroadcaster.getPreviewSizeList();


        if (sizeList != null && sizeList.size() > 0) {
            mCameraResolutionsDialog = new CameraResolutionsFragment();

            mCameraResolutionsDialog.setCameraResolutions(sizeList, mLiveVideoBroadcaster.getPreviewSize());
            mCameraResolutionsDialog.show(ft, "resolutiton_dialog");
        }
        else {
            Snackbar.make(mRootView, "No resolution available",Snackbar.LENGTH_LONG).show();
        }

    }

    public void toggleBroadcasting(View v) {
        if (!mIsRecording)
        {
            if (mLiveVideoBroadcaster != null) {
                if (!mLiveVideoBroadcaster.isConnected()) {
//                    String streamName = mStreamNameEditText.getText().toString();

                    new AsyncTask<String, String, Boolean>() {
                        ContentLoadingProgressBar
                                progressBar;
                        @Override
                        protected void onPreExecute() {
                            progressBar = new ContentLoadingProgressBar(ActivityPushRtmp.this);
                            progressBar.show();
                        }

                        @Override
                        protected Boolean doInBackground(String... url) {
                            return mLiveVideoBroadcaster.startBroadcasting(url[0]);

                        }

                        @Override
                        protected void onPostExecute(Boolean result) {
                            progressBar.hide();
                            mIsRecording = result;
                            if (result) {
//                                mPushStatus.setVisibility(View.VISIBLE);

                                mStartPushButton.setText(R.string.stop_push);
//                                mSettingsButton.setVisibility(View.GONE);
                                startTimer();//start the recording duration
                            }
                            else {
                                triggerStopRecording();
                            }
                        }
                    }.execute(RTMP_URL);
                }
                else {
                    Snackbar.make(mRootView, R.string.streaming_not_finished, Snackbar.LENGTH_LONG).show();
                }
            }
            else {
                Snackbar.make(mRootView, R.string.oopps_shouldnt_happen, Snackbar.LENGTH_LONG).show();
            }
        }
        else
        {
            triggerStopRecording();
        }

    }

    public void triggerStopRecording() {
        if (mIsRecording) {
            mStartPushButton.setText(R.string.start_push);

//            mPushStatus.setVisibility(View.GONE);
//            mPushStatus.setText(R.string.in_the_push);
//            mSettingsButton.setVisibility(View.VISIBLE);

            stopTimer();
            mLiveVideoBroadcaster.stopBroadcasting();
        }

        mIsRecording = false;
    }

    //This method starts a mTimer and updates the textview to show elapsed time for recording
    public void startTimer() {

        if(mTimer == null) {
            mTimer = new Timer();
        }

        mElapsedTime = 0;
        mTimer.scheduleAtFixedRate(new TimerTask() {

            public void run() {
                mElapsedTime += 1; //increase every sec
                mTimerHandler.obtainMessage(TimerHandler.INCREASE_TIMER).sendToTarget();

                if (mLiveVideoBroadcaster == null || !mLiveVideoBroadcaster.isConnected()) {
                    mTimerHandler.obtainMessage(TimerHandler.CONNECTION_LOST).sendToTarget();
                }
            }
        }, 0, 1000);
    }


    public void stopTimer()
    {
        if (mTimer != null) {
            this.mTimer.cancel();
        }
        this.mTimer = null;
        this.mElapsedTime = 0;
    }

    public void setResolution(Resolution size) {
        mLiveVideoBroadcaster.setResolution(size);
    }

    private class TimerHandler extends Handler {
        static final int CONNECTION_LOST = 2;
        static final int INCREASE_TIMER = 1;

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INCREASE_TIMER:
//                    mPushStatus.setText(getString(R.string.in_the_push) + " - " + getDurationString((int) mElapsedTime));
                    break;
                case CONNECTION_LOST:
                    triggerStopRecording();
                    Dialog dialog = new AlertDialog.Builder(ActivityPushRtmp.this)
                            .setMessage(R.string.broadcast_connection_lost)
                            .setPositiveButton(android.R.string.yes, null)
                            .show();
//
//                    dialog.show();

                    break;
            }
        }
    }

    public static String getDurationString(int seconds) {

        if(seconds < 0 || seconds > 2000000)//there is an codec problem and duration is not set correctly,so display meaningfull string
            seconds = 0;
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        seconds = seconds % 60;

        if(hours == 0)
            return twoDigitString(minutes) + " : " + twoDigitString(seconds);
        else
            return twoDigitString(hours) + " : " + twoDigitString(minutes) + " : " + twoDigitString(seconds);
    }

    public static String twoDigitString(int number) {

        if (number == 0) {
            return "00";
        }

        if (number / 10 == 0) {
            return "0" + number;
        }

        return String.valueOf(number);
    }
}
