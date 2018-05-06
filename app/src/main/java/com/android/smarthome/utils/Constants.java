package com.android.smarthome.utils;

/**
 * Created by stevyang on 2017/8/28.
 */

public class Constants {

    public boolean isDebug = true;
    public boolean isRed5 = true;



    //MINA 业务服务器地址
    //test IP and port
//    public static final String IP_ADDRESS = "192.168.2.103";
//    public static final int PORT = 8000;

    //正式IP and port
    public static final String IP_ADDRESS = "119.23.240.132"; //"192.168.1.106";
    public static final int PORT = 8000;

    //OPEN GLEG rtmp视频播放地址
//    String serverUrl = "rtmp://119.23.240.132:1935/live/12345";
    public static final String serverUrl = "rtmp://192.168.2.103:1935/oflaDemo/stream12345 live=1";


    public static final String USER_INFO = "userInfo";

    public static final class CommonInfo {
        public static final String DETAIL_URL = "detail_url";
    }

    public static final int ASR_RESULT_MSG = 10000;
    public static final class DeviceInfo {
        public static final String DEVICE_TYPE = "device_type";
        public static final String DEVICE_MAC = "device_mac";
        public static final String DEVICE_MSG = "device_msg";
    }

    public static final class UserInfo {
        public static final String IS_LOGIN = "isLogin";
        public static final String USER_NAME = "user_name";
        public static final String USER_PHONE = "user_phone";
        public static final String USER_PASSWORD = "user_password";
        public static final String USER_AVATAR = "user_avatar";
        public static final String USER_SIGNATURE = "user_signature";
    }
}
