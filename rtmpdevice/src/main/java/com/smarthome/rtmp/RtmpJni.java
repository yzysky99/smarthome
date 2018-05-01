package com.smarthome.rtmp;


public final class RtmpJni {
    static {
        System.loadLibrary("rtmp_device");
    }

    private long ptClass;
    private long timeOffset;
    public RtmpJni(){}

    public int initRtmp(String url, int width, int height, int timeOut) {
        ptClass = nativeInitRtmp(url, width, height, timeOut);
        if (ptClass != 0) {
            return 0;
        }
        return -1;
    }

    public int pushSpsPpsData(byte[] sps, int spsLen, byte[] pps, int ppsLen, long timeOffset) {
        this.timeOffset = timeOffset;
        return nativePushSpsPpsData(ptClass, sps, spsLen, pps, ppsLen, 0);
    }

    public int pushVideoData(byte[] data, int len, long timestamp) {
        if(timestamp - timeOffset <= 0){
            return -1;
        }
        return nativePushVideoData(ptClass, data, len, timestamp - timeOffset);
    }

    public int pushAudioSpec(byte[] data, int len) {
        return nativePushAudioSpec(ptClass, data, len);
    }

    public int pushAudioData(byte[] data, int len, long timestamp) {
        if(timestamp-timestamp < 0){
            return -1;
        }
        return nativePushAudioData(ptClass, data, len, timestamp - timeOffset);
    }

    public int stopRtmp() {
        try {
            return nativeStopRtmp(ptClass);
        }finally {
            ptClass=0;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        if(ptClass!=0){
            stopRtmp();
        }
    }

    static native long nativeInitRtmp(String url, int width, int height, int timeOut);
    static native int nativePushSpsPpsData(long ptClass, byte[] sps, int spsLen, byte[] pps,
                                    int ppsLen, long timestamp);
    static native int nativePushVideoData(long ptClass, byte[] data, int len, long timestamp);
    static native int nativePushAudioSpec(long ptClass, byte[] data, int len);
    static native int nativePushAudioData(long ptClass, byte[] data, int len, long timestamp);
    static native int nativeStopRtmp(long ptClass);
}