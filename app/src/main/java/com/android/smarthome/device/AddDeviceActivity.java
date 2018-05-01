package com.android.smarthome.device;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.android.smarthome.R;
import com.google.zxing.activity.CaptureActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class AddDeviceActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "Msg.AddDeviceActivity";

    Button qrBtn;
    Button autoBtn;
    Button softAPBtn;
    Button manualBtn;
    private static final  int REQUEST_QR_CAMERA = 100;
    private final static int REQ_QR_CODE = 101;
    private final static int REQ_AUTO_CODE = 102;
    private final static int REQ_SOFTAP_CODE = 103;
    private final static int REQ_MANUAL_CODE = 103;

    public static final int REQUSET = 1;

    public static List<String> img_list = new ArrayList<String>();
    public static List<String> id_list = new ArrayList<String>();
    public static List<String> key_list = new ArrayList<String>();
    public static List<String> name_list = new ArrayList<String>();
    public static List<String> stat_list = new ArrayList<String>();

    private SwipeRefreshLayout swipeRefreshLayout;

    private ProgressDialog pd;
    Timer timer = new Timer();
    public static boolean pause_flag=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_device);
        initView();
        initData();
    }

    private void initView() {
        qrBtn = (Button) findViewById(R.id.qr_code_add);
        autoBtn = (Button) findViewById(R.id.auto_add);
        softAPBtn = (Button) findViewById(R.id.softAP_add);
        manualBtn = (Button) findViewById(R.id.manual_add);
    }

    private void initData() {
        qrBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"qrBtn click!");
                AndPermission.with(AddDeviceActivity.this)
                        .requestCode(REQUEST_QR_CAMERA)
                        .permission(Manifest.permission.CAMERA).rationale(new RationaleListener() {

                    @Override
                    public void showRequestPermissionRationale(int arg0, Rationale arg1) {
                        AndPermission.rationaleDialog(AddDeviceActivity.this, arg1).show();
                    }
                }).send();
            }
        });

        autoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDeviceActivity.this, AutoConfigWifiActivity.class);
                startActivityForResult(intent, REQ_AUTO_CODE);
            }
        });

        softAPBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Intent intent = new Intent(AddDeviceActivity.this, SoftAPConfigActivity.class);
//                startActivityForResult(intent, REQ_SOFTAP_CODE);

                String ssid = getConnectWifiSsid();
//                if(ssid.indexOf("选择热点Smart_Home") != -1){
                    Intent intent2 = new Intent(AddDeviceActivity.this, SoftAPConfigActivity.class);
                    startActivity(intent2);
//                }else{
//                    new AlertDialog.Builder(AddDeviceActivity.this)
//                            .setIcon(android.R.drawable.ic_dialog_info)
//                            .setTitle("提示")
//                            .setMessage("请进入手机wifi设置, 选择热点Smart_Home, 完成后再次进入AP模式添加！")
//                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            })
//                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                                public void onClick(DialogInterface dialog, int whichButton) {
//
//                                    if(android.os.Build.VERSION.SDK_INT > 10 ){
//                                        startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
//                                    } else {
//                                        startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
//                                    }
//
//                                    dialog.dismiss();
//
//                                    pause_flag=true;
//                                }
//                            }).show();
//
//                }
            }
        });

        manualBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(AddDeviceActivity.this, ManualActivity.class);
                startActivityForResult(intent, REQ_MANUAL_CODE);
            }
        });

        new setdata().start();
    }


    private String getConnectWifiSsid() {
        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo.getSSID();
    }

    public boolean isNetworkAvailable(Activity activity)
    {
        Context context = activity.getApplicationContext();
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null)
        {
            return false;
        }
        else
        {
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0)
            {
                for (int i = 0; i < networkInfo.length; i++)
                {
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AndPermission.onRequestPermissionsResult(requestCode, permissions,grantResults, reqPermissionListener);
    }

    PermissionListener reqPermissionListener = new PermissionListener() {

        @Override
        public void onSucceed(int requestCode, List<String> grantPermissions) {
            if (requestCode == REQUEST_QR_CAMERA) {
                Intent intent = new Intent(AddDeviceActivity.this, CaptureActivity.class);
                startActivityForResult(intent, REQ_QR_CODE);
            }
        }

        @Override
        public void onFailed(int requestCode, List<String> deniedPermissions) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_QR_CODE) {
            if (data != null) {
                String result = data.getStringExtra(CaptureActivity.SCAN_QRCODE_RESULT);
                Log.d(TAG,"扫码结果：" + result);
            }

            if (requestCode == REQ_QR_CODE && resultCode == RESULT_OK) {
                if(data.getStringExtra("RESULT").equals("1")){
                    new setdataThree().start();
                }else{

                }
            }
        }
    }

    private String encode(String src){
        String des= Uri.encode(src);
        return des;
    }

    @Override
    protected void onResume() {
        super.onResume();

        String ssid = getConnectWifiSsid();
        if(ssid.indexOf("Doit_ESP") == -1 && isNetworkAvailable(AddDeviceActivity.this)){
            pause_flag=false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }



    private class setdata extends Thread{
        public void run() {

            Message msg1=new Message();
            msg1.what=0;
            myHandler.sendMessage(msg1);

            img_list.clear();
            id_list.clear();
            key_list.clear();
            name_list.clear();
            stat_list.clear();

            try {
                String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+ encode("open_id")+
                        "&app_id=155";

//                MyHttp myGet = new MyHttp(uri);
//                String des =  myGet.httpPost();
                String des = "";

                if(!des.equals("null")){
                    JSONArray array = new JSONArray(des);
                    int num = array.length();

                    for(int j=0;j<num;j++){
                        JSONObject jsonEvents = array.getJSONObject(j);

                        img_list.add(jsonEvents.getString("device_img"));
                        id_list.add(jsonEvents.getString("device_id"));
                        key_list.add(jsonEvents.getString("device_key"));
                        name_list.add(jsonEvents.getString("device_name"));
                        stat_list.add(jsonEvents.getString("device_stat"));
                    }

                    myHandler.sendEmptyMessageDelayed(1,2000);
                }else{
                    Message msg=new Message();
                    msg.what=-11;
                    myHandler.sendMessage(msg);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();

                Message msg=new Message();
                msg.what=-1;
                myHandler.sendMessage(msg);

            }


        }
    }

    private class setdataTwo extends Thread{
        public void run() {
            img_list.clear();
            id_list.clear();
            key_list.clear();
            name_list.clear();
            stat_list.clear();

            try {
                String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+encode("open_id")+
                        "&app_id=155";

//                MyHttp myGet = new MyHttp(uri);
//                String des =  myGet.httpPost();
                String des = "";

                if(!des.equals("null")){
                    JSONArray array = new JSONArray(des);
                    int num = array.length();

                    for(int j=0;j<num;j++){
                        JSONObject jsonEvents = array.getJSONObject(j);
                        img_list.add(jsonEvents.getString("device_img"));
                        id_list.add(jsonEvents.getString("device_id"));
                        key_list.add(jsonEvents.getString("device_key"));
                        name_list.add(jsonEvents.getString("device_name"));
                        stat_list.add(jsonEvents.getString("device_stat"));
                    }

                    myHandler.sendEmptyMessageDelayed(3,2000);
                }else{
                    Message msg=new Message();
                    msg.what=-3;
                    myHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                myHandler.sendEmptyMessageDelayed(-3,2000);
//   	    	           Message msg=new Message();
//   		    		   msg.what=-3;
//   		    		   myHandler.sendMessage(msg);
            }
        }
    }

    private class setdataThree extends Thread{
        public void run() {
            img_list.clear();
            id_list.clear();
            key_list.clear();
            name_list.clear();
            stat_list.clear();
            try {
                String uri = "http://wechat.doit.am/iot_api/list.php?open_id="+encode("open_id")+
                        "&app_id=155";

//                MyHttp myGet = new MyHttp(uri);
//                String des =  myGet.httpPost();
                String des = "";

                if(!des.equals("null")){
                    JSONArray array = new JSONArray(des);
                    int num = array.length();

                    for(int j=0;j<num;j++){
                        JSONObject jsonEvents = array.getJSONObject(j);

                        img_list.add(jsonEvents.getString("device_img"));
                        id_list.add(jsonEvents.getString("device_id"));
                        key_list.add(jsonEvents.getString("device_key"));
                        name_list.add(jsonEvents.getString("device_name"));
                        stat_list.add(jsonEvents.getString("device_stat"));
                    }
                    Message msg=new Message();
                    msg.what=4;
                    myHandler.sendMessage(msg);
                }else{
                    Message msg=new Message();
                    msg.what=-3;
                    myHandler.sendMessage(msg);
                }

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Message msg=new Message();
                msg.what=-4;
                myHandler.sendMessage(msg);
            }
        }
    }

    @Override
    public void onRefresh() {
        new setdataTwo().start();
    }

    @SuppressLint("HandlerLeak")
    Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
            switch(msg.what){
                case -11:
                    pd.dismiss();
                    break;
                case -1:
                    pd.dismiss();
                    Toast.makeText(AddDeviceActivity.this,"获取设备失败，请重新尝试！",Toast.LENGTH_LONG).show();
//				adapter.notifyDataSetChanged();
                    break;
                case 0:
                    pd = ProgressDialog.show(AddDeviceActivity.this, "", "正在从云端加载列表 ...");
                    break;
                case 1:
                    reload();
//                    adapter.notifyDataSetChanged();
                    pd.dismiss();
                    UpdateDevice();
                    break;
                case 2:
//        	    reload();
//                    adapter.notifyDataSetChanged();
                    break;
                case -2:
//				Toast.makeText(ListCarActivity.this,"更新失败，请重新尝试！",Toast.LENGTH_LONG).show();
                    break;
                case 3:
                    reload();
//                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                    break;
                case -3:
                    swipeRefreshLayout.setRefreshing(false);
//				Toast.makeText(ListCarActivity.this,"更新失败，请重新尝试！",Toast.LENGTH_LONG).show();
                    break;
                case 4:
                    reload();
//                    adapter.notifyDataSetChanged();
                    break;
                case -4:
                    break;
            }
        }
    };

    private void reload(){
//        adapter.clean();
        int len= img_list.size();
        for(int i=0;i<len;i++){
//            adapter.addBook(img_list.get(i),id_list.get(i),key_list.get(i),name_list.get(i),stat_list.get(i));
        }
    }


    private void UpdateDevice(){

        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                if(!pause_flag){
                    String res = null;
                    int len  = stat_list.size();
                    int num = 0;
                    for(int i=0;i<len;i++){
                        try {
//                            MyHttp myDol = new MyHttp("http://wechat.myembed.com/cloud_api/num.php?device_id=" +id_list.get(i)+ "&device_key="+key_list.get(i));
//                            res = myDol.httpPost();


                            if(!stat_list.get(i).equals(res)){
                                stat_list.set(i,res);
//                                adapter.setModel(img_list.get(i),id_list.get(i),key_list.get(i),name_list.get(i),stat_list.get(i),i);
                                num++;
                            }
                        } catch (Exception e) {
                            i=len;
                            num=0;
                            break;
                        }
                    }

                    if(num>0){
                        Message msg=new Message();
                        msg.what=2;
                        myHandler.sendMessage(msg);
                    }
                }

            }
        }, 0,2000);
    }

}
