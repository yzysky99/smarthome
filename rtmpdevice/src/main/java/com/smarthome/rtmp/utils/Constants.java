package com.smarthome.rtmp.utils;

import android.media.AudioFormat;

/**
 * Created by stevyang on 18/3/6.
 */

public class Constants {
    public static final String RTMP_URL = "rtmp://119.23.240.132:1935/live/12345";
    public static final int RTMP_FPS = 30;

    public static final int RTMP_MIN_WIDTH = 320;
    public static final int RTMP_MAX_WIDTH = 1080;


    public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    public static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
    public static final int SAMPLE_RATE = 44100;

    public static final int BIT_RATE = 700 * 1000;

    public static final int RTMP_TIMEOUT = 1000;

}
