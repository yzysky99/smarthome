#include "rtmp_jni.h"

#ifdef __cplusplus
extern "C" {
#endif

JNIEXPORT jlong JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativeInitRtmp(JNIEnv *env, jclass type, jstring url, jint width, jint height,
                                         jint timeOut) {

    const char *c_url = env->GetStringUTFChars(url, 0);
    RtmpInterface *rtmpInterface = new RtmpInterface();
    rtmpInterface->initRtmp(c_url, width, height, timeOut);

    env->ReleaseStringUTFChars(url, c_url);
    return reinterpret_cast<long> (rtmpInterface);
}


JNIEXPORT jint JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativePushSpsPpsData(JNIEnv *env, jclass type, jlong ptClass,
                                                  jbyteArray sps, jint spsLen, jbyteArray pps,
                                                  jint ppsLen, jlong timestamp) {
    jbyte *spsBytes = env->GetByteArrayElements(sps, NULL);
    jbyte *ppsBytes = env->GetByteArrayElements(pps, NULL);
    RtmpInterface *rtmpInterface = reinterpret_cast<RtmpInterface *>(ptClass);
    int ret = rtmpInterface->pushSpsPpsData((BYTE *) spsBytes, spsLen, (BYTE *) ppsBytes, ppsLen, timestamp);

    env->ReleaseByteArrayElements(sps, spsBytes, 0);
    env->ReleaseByteArrayElements(pps, ppsBytes, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativePushVideoData(JNIEnv *env, jclass type, jlong ptClass,
                                                  jbyteArray data, jint len, jlong timestamp) {
    jbyte *dataBytes = env->GetByteArrayElements(data, NULL);
    RtmpInterface *rtmpInterface = reinterpret_cast<RtmpInterface *> (ptClass);
    int ret = rtmpInterface->pushVideoData((BYTE *) dataBytes, len, timestamp);

    env->ReleaseByteArrayElements(data, dataBytes, 0);

    return ret;
}

JNIEXPORT jint JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativePushAudioSpec(JNIEnv *env, jclass type, jlong ptClass,
                                                jbyteArray data, jint len) {
    jbyte *dataBytes = env->GetByteArrayElements(data, NULL);

    RtmpInterface *rtmpInterface = reinterpret_cast<RtmpInterface *> (ptClass);
    int ret = rtmpInterface->pushAudioSpec((BYTE *) dataBytes, len);

    env->ReleaseByteArrayElements(data, dataBytes, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativePushAudioData(JNIEnv *env, jclass type, jlong ptClass,
                                                jbyteArray data, jint len, jlong timestamp) {
    jbyte *dataBytes = env->GetByteArrayElements(data, NULL);

    RtmpInterface *rtmpInterface = reinterpret_cast<RtmpInterface *> (ptClass);
    int ret = rtmpInterface->pushAudioData((BYTE *) dataBytes, len, timestamp);

    env->ReleaseByteArrayElements(data, dataBytes, 0);
    return ret;
}

JNIEXPORT jint JNICALL
Java_com_smarthome_rtmp_RtmpJni_nativeStopRtmp(JNIEnv *env, jclass type, jlong ptClass) {
    RtmpInterface *rtmpInterface = reinterpret_cast<RtmpInterface *> (ptClass);
    delete rtmpInterface;
    return 0;
}

#ifdef __cplusplus
}
#endif