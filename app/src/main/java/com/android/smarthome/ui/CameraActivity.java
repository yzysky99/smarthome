package com.android.smarthome.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.smarthome.MyApplication;
import com.android.smarthome.R;
import com.android.smarthome.rtmpplayer.PlayerActivity;
import com.android.smarthome.utils.Utils;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraActivity extends AppCompatActivity {
    private static final String TAG = "CameraActivity";

//    private EditText ipAddressEdit;
    private Button openCameraBtn;
    public static String cameraIp;

//    private EditText remoteIpAddressEdit;
//    private EditText remoteIpPortEdit;
    private Button remoteCameraBtn;
    public String remoteCameraIp;
    public int remoteCameraPort;

    private TextView msgText = null;
    private ScrollView scrollView = null;

    public static Handler msgHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        initView();
        initData();
    }

    private void initView() {
//        ipAddressEdit = (EditText)findViewById(R.id.ipAddress);
        openCameraBtn = (Button)findViewById(R.id.camera_open);

//        remoteIpAddressEdit = (EditText)findViewById(R.id.remoteIPAddress);
//        remoteIpPortEdit = (EditText)findViewById(R.id.remoteIpPort);
        remoteCameraBtn = (Button)findViewById(R.id.remote_camera_open);

//        cameraIp = ipAddressEdit.getText().toString();
//        remoteCameraIp = remoteIpAddressEdit.getText().toString();
//        remoteCameraPort = Integer.parseInt(remoteIpPortEdit.getText().toString());

        msgText = (TextView) findViewById(R.id.text_msg);
        scrollView = (ScrollView) findViewById(R.id.stroll_text);

    }

    private void initData() {

        // 启动百度push
        PushManager.startWork(getApplicationContext(), PushConstants.LOGIN_TYPE_API_KEY,
                Utils.getMetaValue(CameraActivity.this, "api_key"));

//        openCameraBtn.setOnClickListener(new Button.OnClickListener()
//        {
//            public void onClick(View v) {
//                Intent intent = new Intent(getApplication(), CameraSurfaceActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("CameraIp", cameraIp);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });
//
//        remoteCameraBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(getApplication(), RemoteCameraActivity.class);
//                Bundle bundle = new Bundle();
//                bundle.putString("remoteCameraIp", remoteCameraIp);
//                bundle.putInt("remoteCameraPort", remoteCameraPort);
//                intent.putExtras(bundle);
//                startActivity(intent);
//            }
//        });



        openCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String demoUrl = "rtmp://live.hkstv.hk.lxdns.com/live/hks";

                Intent intent = new Intent(CameraActivity.this, PlayerActivity.class);
                intent.setData(Uri.parse(demoUrl));
                intent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS, false);
                intent.setAction(PlayerActivity.ACTION_VIEW);
                startActivity(intent);
            }
        });

        remoteCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String serverUrl = "rtmp://119.23.240.132:1935/live/12345";

                Intent intent = new Intent(CameraActivity.this, PlayerActivity.class);
                intent.setData(Uri.parse(serverUrl));
                intent.putExtra(PlayerActivity.PREFER_EXTENSION_DECODERS, false);
                intent.setAction(PlayerActivity.ACTION_VIEW);
                startActivity(intent);
            }
        });


        msgHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }
        };
    }

    protected void handleMsg(Message msg) {
        Log.d(TAG, "get msg: " + msg);
        if (msgText != null && msg.obj != null) {
            msgText.append(msg.obj.toString() + "\n");
        }
    }

    // 更新界面显示内容
    private void updateDisplay() {
        Log.d(TAG, "updateDisplay, logText:" + msgText + " cache: "
                + Utils.logStringCache);
        if (msgText != null) {
            msgText.setText(Utils.logStringCache);
        }
        if (scrollView != null) {
            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();
        updateDisplay();
    }

    @Override
    protected void onDestroy() {
        Utils.setLogText(getApplicationContext(), Utils.logStringCache);
        super.onDestroy();
        msgHandler = null;
    }
}
