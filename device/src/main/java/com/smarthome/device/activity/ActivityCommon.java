package com.smarthome.device.activity;

import android.Manifest;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import com.smarthome.device.R;
import com.smarthome.device.pushRtmp.ActivityPushRtmp;
import com.smarthome.device.network.TcpClient;
import com.smarthome.device.util.Logger;
import com.smarthome.device.recognization.online.InFileStream;
import com.smarthome.device.utils.Utils;
import com.smarthome.device.view.ToastView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.smarthome.device.utils.Constants.ASR_RESULT_MSG;
import static com.smarthome.device.utils.Constants.DEVICE_CMD;
import static com.smarthome.device.utils.Constants.DEVICE_DATA;
import static com.smarthome.device.utils.Constants.DEVICE_ID;
import static com.smarthome.device.utils.Constants.DEVICE_MSG;
import static com.smarthome.device.utils.Constants.MSG_TYPE;
import static com.smarthome.device.utils.Utils.getWifiMac;

/**
 * Created by fujiayi on 2017/6/20.
 */

public abstract class ActivityCommon extends AppCompatActivity {
    private static final String TAG = "ActivityCommon";

    protected TextView txtLog;
    protected Button btn;
//    protected Button setting;
    protected TextView txtResult;
    protected Handler handler;
    protected String DESC_TEXT;
    protected int layout = R.layout.common;

    protected Class settingActivityClass = null;

    protected boolean running = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStrictMode();
        InFileStream.setContext(this);
        setContentView(layout);
        initView();
        handler = new Handler() {

            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                handleMsg(msg);
            }

        };
        Logger.setHandler(handler);
        initPermission();
        initRecog();
    }


    protected abstract void initRecog();

    private void sendData(String data) {
        String macStr = Utils.getWifiMac(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONObject deviceMsg = new JSONObject();
            deviceMsg.put(DEVICE_CMD, 201);//sendData
            deviceMsg.put(DEVICE_ID, macStr);
            deviceMsg.put(DEVICE_DATA, data);

            jsonObject.put(MSG_TYPE, "device");
            jsonObject.put(DEVICE_MSG, deviceMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d(TAG,"sendData: " + jsonObject.toString());

        if (data.equals("耳朵")) {
            Intent intent = new Intent(ActivityCommon.this, ActivityPushRtmp.class);
            intent.putExtra("source", 1);
            startActivity(intent);
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client = new TcpClient();
                client.sendData(jsonObject.toString(), new TcpClient.IDataRecvListener() {
                    @Override
                    public int onDataRecv(String data) {
                        Log.d(TAG,"recv data: " + data);
                        boolean sendData = false;
                        if (data != null) {
                            try {
                                JSONObject respData = new JSONObject(data);
                                String status = respData.getString("status");
                                String deviceId = respData.getString("device_id");
                                String name = respData.getString("name");
                                if (status != null && status.equals("success")) {
                                    sendData = true;
                                } else if (status != null && status.equals("fail")) {
                                    sendData = false;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

//                        if (!sendData) {
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ToastView toast = new ToastView(ActivityDevice.this, getString(R.string.get_device_info_failed));
//                                    toast.setGravity(Gravity.CENTER, 0, 0);
//                                    toast.show();
//                                }
//                            });
//                        }
                        return 0;
                    }
                });
            }
        }).start();
    }

    protected void handleMsg(Message msg) {
        Log.d(TAG, "get msg: " + msg);
        if (txtLog != null && msg.obj != null) {
            txtLog.append(msg.obj.toString() + "\n");

            if (msg.what == ASR_RESULT_MSG) {
                sendData(msg.obj.toString());
            }
        }
    }

    protected void initView() {
        txtResult = (TextView) findViewById(R.id.txtResult);
        txtLog = (TextView) findViewById(R.id.txtLog);
        btn = (Button) findViewById(R.id.btn);
//        setting = (Button) findViewById(R.id.setting);
        txtLog.setText(DESC_TEXT + "\n");
//        if (setting != null && settingActivityClass != null) {
//            setting.setOnClickListener(new View.OnClickListener() {
//
//                @Override
//                public void onClick(View v) {
//                    running = true;
//                    Intent intent = new Intent(ActivityCommon.this, settingActivityClass);
//                    startActivityForResult(intent, 1);
//                }
//            });
//        }

    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String permissions[] = {Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm :permissions){
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()){
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }

    private void setStrictMode() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

    }
}
