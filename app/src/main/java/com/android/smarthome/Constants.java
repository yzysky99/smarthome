package com.android.smarthome;

/**
 * Created by stevyang on 2017/8/28.
 */

public class Constants {

    //test IP and port
    public static final String IP_ADDRESS = "192.168.0.103";
    public static final int PORT = 8000;

    //正式IP and port
//    public static final String IP_ADDRESS = "119.23.240.132"; //"192.168.1.106";
//    public static final int PORT = 8000;

    public static final String USER_INFO = "userInfo";

    public static final class CommonInfo {
        public static final String DETAIL_URL = "detail_url";
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
