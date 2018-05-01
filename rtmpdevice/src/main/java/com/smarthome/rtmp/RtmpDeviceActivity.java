package com.smarthome.rtmp;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;


public class RtmpDeviceActivity extends AppCompatActivity{

    private static final String TAG = "RtmpDeviceActivity";

    private Button btnStartPush;
//    private Button btnChangeCamera;
    private SurfaceView mSurfaceView;

    private SurfaceHolder mSurfaceHolder;
    private boolean isPushStart;

    private RtmpPush mRtmpPush;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate!");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_rtmpdevice);
        initView();
        initData();
    }

    private void initView() {
        btnStartPush = (Button) findViewById(R.id.btn_start_push);
//        btnChangeCamera = (Button) findViewById(R.id.btn_change_camera);
        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
    }

    private void initData() {
        mSurfaceView.setKeepScreenOn(true);
        mSurfaceHolder = mSurfaceView.getHolder();
//        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.addCallback(new SurfaceHolder.Callback2() {
            @Override
            public void surfaceRedrawNeeded(SurfaceHolder holder) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                Log.i(TAG, "surfaceChanged!");
                mRtmpPush.initVideoDevice(RtmpDeviceActivity.this, holder);
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });

        btnStartPush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                swithRtmpPush();
            }
        });

//        btnChangeCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        mRtmpPush = new RtmpPush();
        mRtmpPush.init();
    }

    private void swithRtmpPush() {
        if (isPushStart) {
            stopRtmpPush();
        } else {
            startRtmpPush();
        }
    }

    private void startRtmpPush() {
        Log.d(TAG,"start rtmp push!");
        //初始化声音采集
        mRtmpPush.initAudioDevice();
        //初始化编码器
        mRtmpPush.initEncoders();
        //开始采集
        mRtmpPush.startRecordAudioData();
        //开始编码
        mRtmpPush.startEncoder();
        //开始推送
        mRtmpPush.starPublish();
        isPushStart = true;
        btnStartPush.setText(getString(R.string.stop_push));
    }

    private void stopRtmpPush() {
        Log.d(TAG,"stop rtmp push!");
        mRtmpPush.stopPublish();
        mRtmpPush.stopGather();
        mRtmpPush.stopEncoder();
        isPushStart = false;
        btnStartPush.setText(getString(R.string.start_push));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        int cameraPer = ActivityCompat.checkSelfPermission(RtmpDeviceActivity.this, Manifest.permission.CAMERA);
        int audioPer =ActivityCompat.checkSelfPermission(RtmpDeviceActivity.this,Manifest.permission.RECORD_AUDIO);
        if (cameraPer == PermissionChecker.PERMISSION_GRANTED
                && audioPer == PermissionChecker.PERMISSION_GRANTED) {

//            startActivity(new Intent(RtmpDeviceActivity.this, MainActivity.class));
        } else {
            ActivityCompat.requestPermissions(RtmpDeviceActivity.this, new String[]{Manifest.permission.CAMERA
                    ,Manifest.permission.RECORD_AUDIO}, 0);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume!");
        mRtmpPush.initVideoDevice(this, mSurfaceHolder);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause!");
        stopRtmpPush();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy!");
        mRtmpPush.release();
    }

}
