package com.smarthome.device.utils;

import android.media.AudioFormat;

/**
 * Created by stevyang on 2017/8/28.
 */

public class Constants {

    public boolean isDebug = true;
    public boolean isRed5 = true;

    //test IP and port
    public static final String IP_ADDRESS = "192.168.0.103";
    public static final int PORT = 8000;

    //正式IP and port
//    public static final String IP_ADDRESS = "119.23.240.132"; //"192.168.1.105";
//    public static final int PORT = 8100;

//    public static final String RTMP_URL = "rtmp://119.23.240.132:1935/live/12345";
    public static final String RTMP_URL = "rtmp://192.168.2.103:1935/oflaDemo/stream12345";


    public static final int ASR_RESULT_MSG = 10000;


    public static final String USER_INFO = "userInfo";

    public static final class CommonInfo {
        public static final String DETAIL_URL = "detail_url";
    }

//    public static final class DeviceInfo {
//        public static final String deviceMac = "mac";
//        public static final String deviceName = "name";
//        public static final String deviceRtmpUrl = "rtmp_url";
//        public static final String deviceUsers = "users";
//    }

    public static final String DEVICE_ID = "device_id";
    public static final String DEVICE_CMD = "cmd";
    public static final String MSG_TYPE = "msgType";
    public static final String DEVICE_MSG = "device";

    public static final String DEVICE_DATA = "data";


}
