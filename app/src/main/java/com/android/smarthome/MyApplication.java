package com.android.smarthome;

import android.app.Application;
import android.os.Handler;
import android.util.Log;

import com.android.smarthome.crash.CrashHandler;

/**
 * Created by stevyang on 2017/10/26.
 */

public class MyApplication extends Application {
    public static final String TAG = "MyApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = new CrashHandler();
        crashHandler.init(this);

        Log.d(TAG, "Application is onCreated!");
        Log.e(TAG, "*****UfoSDK.init(this)*****");

//        UfoSDK.init(this);
//        UfoSDK.openRobotAnswer();
//
//        // 设置用户的头像
//        UfoSDK.setCurrentUserIcon(getMeIconBitmap());
//        // 在聊天界面中获取聊天信息的时间间隔
//        UfoSDK.setChatThreadTime(10);
//        // 设置当前用户名
//        UfoSDK.setBaiduCuid(DeviceId.getCUID(this));
//        // 我的反馈按钮颜色
//        UfoSDK.setRootBackgroundColor(getResources().getColor(R.color.gray));

    }
}
