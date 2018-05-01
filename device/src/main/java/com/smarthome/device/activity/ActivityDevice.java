package com.smarthome.device.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;

import com.smarthome.device.R;
import com.smarthome.device.network.TcpClient;
import com.smarthome.device.pushRtmp.ActivityPushRtmp;
import com.smarthome.device.utils.Utils;
import com.smarthome.device.view.ToastView;
import com.smarthome.rtmp.RtmpDeviceActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.smarthome.device.utils.Constants.DEVICE_CMD;
import static com.smarthome.device.utils.Constants.DEVICE_ID;
import static com.smarthome.device.utils.Constants.DEVICE_MSG;
import static com.smarthome.device.utils.Constants.MSG_TYPE;

public class ActivityDevice extends AppCompatActivity {
    private static final String TAG = "ActivityDevice";
    private Button voiceBtn;
    private Button videoBtn;
    private Button pushBtn;
//    private Button playBtn;
    private Button pushNewBtn;

    private String mDeviceID;
    private String mDeviceName;
    private String mDeviceRtmpUrl;
    private String mDeviceUsers;

    List<String> userList=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device);

        voiceBtn = (Button) findViewById(R.id.open_voice_monitoring);
//        videoBtn = (Button) findViewById(R.id.open_video_monitoring);
        pushBtn = (Button) findViewById(R.id.open_video_push);
//        playBtn = (Button) findViewById(R.id.open_video_play);

        pushNewBtn = (Button) findViewById(R.id.open_video_push_new);

        voiceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityDevice.this, ActivityOnline.class);
                startActivity(intent);
            }
        });

        pushBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityDevice.this, ActivityPushRtmp.class);
                startActivity(i);
            }
        });

        pushNewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ActivityDevice.this, RtmpDeviceActivity.class);
                startActivity(i);
            }
        });

//        playBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(ActivityDevice.this, LiveVideoPlayerActivity.class);
//                startActivity(i);
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        hideBtn();
//        deviceLogin();
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        deviceLogout();
    }

    private void hideBtn() {
        voiceBtn.setVisibility(View.GONE);
        pushBtn.setVisibility(View.GONE);
        pushNewBtn.setVisibility(View.GONE);
    }

    private void showBtn() {
        voiceBtn.setVisibility(View.VISIBLE);
        pushBtn.setVisibility(View.VISIBLE);
        pushNewBtn.setVisibility(View.VISIBLE);
    }

    private void deviceLogout(){
        String macStr = Utils.getWifiMac(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONObject deviceMsg = new JSONObject();
            deviceMsg.put(DEVICE_CMD, 202);//logout
            deviceMsg.put(DEVICE_ID, macStr);

            jsonObject.put(MSG_TYPE, "device");
            jsonObject.put(DEVICE_MSG, deviceMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client = new TcpClient();
                client.sendData(jsonObject.toString(), new TcpClient.IDataRecvListener() {
                    @Override
                    public int onDataRecv(String data) {
                        Log.d(TAG,"recv data: " + data);
                        if (data != null) {
                            try {
                                JSONObject respData = new JSONObject(data);
                                String status = respData.getString("status");
                                if (status != null && status.equals("success")) {
                                    Log.d(TAG,"get data success!");
                                } else if (status != null && status.equals("fail")) {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        return 0;
                    }
                });
            }
        }).start();
    }


    private void deviceLogin(){
        String macStr = Utils.getWifiMac(this);
        final JSONObject jsonObject = new JSONObject();
        try {
            JSONObject deviceMsg = new JSONObject();
            deviceMsg.put(DEVICE_CMD, 200);//getInfo
            deviceMsg.put(DEVICE_ID, macStr);

            jsonObject.put(MSG_TYPE, "device");
            jsonObject.put(DEVICE_MSG, deviceMsg);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                TcpClient client = new TcpClient();
                client.sendData(jsonObject.toString(), new TcpClient.IDataRecvListener() {
                    @Override
                    public int onDataRecv(String data) {
                        Log.d(TAG,"recv data: " + data);
                        boolean login = false;
                        if (data != null) {
                            try {
                                JSONObject respData = new JSONObject(data);
                                String status = respData.getString("status");
                                if (status != null && status.equals("success")) {
                                    Log.d(TAG,"get data success!");
                                    JSONObject device = respData.getJSONObject("device");
                                    mDeviceName = device.getString("rtmpUrl");
                                    mDeviceRtmpUrl = device.getString("name");
                                    JSONArray userJsonArray = device.getJSONArray("users");

                                    for (int i = 0; i < userJsonArray.length(); i++) {
                                        String userId = (String) userJsonArray.get(i);
                                        userList.add(userId);
                                    }
                                    showBtn();
                                    login = true;
                                } else if (status != null && status.equals("fail")) {

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }

                        if (!login) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ToastView toast = new ToastView(ActivityDevice.this, getString(R.string.get_device_info_failed));
                                    toast.setGravity(Gravity.CENTER, 0, 0);
                                    toast.show();
                                }
                            });
                        }
                        return 0;
                    }
                });
            }
        }).start();

    }
}
