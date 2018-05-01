#ifndef RTMP_PUSH
#define RTMP_PUSH

#include <string>
#include "string.h"
#include "rtmp_interface.h"
#include "android/log.h"

#ifdef __cplusplus
extern "C" {
#endif
// include c header
#include "rtmp.h"
#include "rtmp_sys.h"
#include "log.h"
#include "android/log.h"
#include "time.h"
#include "stdlib.h"

#ifdef __cplusplus
}
#endif

#define TAG "rtmp_jni"
#define LOGD(fmt, ...) \
        __android_log_print(ANDROID_LOG_DEBUG, TAG,fmt, ##__VA_ARGS__)

#define LOGI(fmt,...) \
        __android_log_print(ANDROID_LOG_INFO, TAG,fmt, ##__VA_ARGS__)


#define BYTE uint8_t

#define RTMP_HEAD_SIZE (sizeof(RTMPPacket)+RTMP_MAX_HEADER_SIZE)
#define NAL_SLICE  1
#define NAL_SLICE_DPA  2
#define NAL_SLICE_DPB  3
#define NAL_SLICE_DPC  4
#define NAL_SLICE_IDR  5
#define NAL_SEI  6
#define NAL_SPS  7
#define NAL_PPS  8
#define NAL_AUD  9
#define NAL_FILLER  12

#define STREAM_CHANNEL_METADATA  0x03
#define STREAM_CHANNEL_VIDEO     0x04
#define STREAM_CHANNEL_AUDIO     0x05

#define AAC_ADTS_HEADER_SIZE 7
#define FLV_TAG_HEAD_LEN 11
#define FLV_PRE_TAG_LEN 4

static const AVal av_onMetaData = AVC("onMetaData");
static const AVal av_duration = AVC("duration");
static const AVal av_width = AVC("width");
static const AVal av_height = AVC("height");
static const AVal av_videocodecid = AVC("videocodecid");
static const AVal av_avcprofile = AVC("avcprofile");
static const AVal av_avclevel = AVC("avclevel");
static const AVal av_videoframerate = AVC("videoframerate");
static const AVal av_audiocodecid = AVC("audiocodecid");
static const AVal av_audiosamplerate = AVC("audiosamplerate");
static const AVal av_audiochannels = AVC("audiochannels");
static const AVal av_avc1 = AVC("avc1");
static const AVal av_mp4a  = AVC("mp4a");
static const AVal av_onPrivateData = AVC("onPrivateData");
static const AVal av_record = AVC("record");

class RtmpInterface {

private:
    RTMP *rtmp;

public:
    /**
     * 初始化
     */
    virtual int initRtmp(std::string url, int w, int h, int timeOut);

    /**
     * 发送sps、pps 帧
     */
    virtual int pushSpsPpsData(BYTE *sps, int spsLen, BYTE *pps, int ppsLen,
                              long timestamp);

    /**
     * 发送视频帧
     */
    virtual int pushVideoData(BYTE *data, int len, long timestamp);

    /**
     * 发送音频关键帧
     */
    virtual int pushAudioSpec(BYTE *data, int len);

    /**
     * 发送音频数据
     */
    virtual int pushAudioData(BYTE *data, int len,long timestamp);

    /**
     * 释放资源
     */
    virtual int stopRtmp() const;

    virtual ~RtmpInterface();
};


#endif //RTMP_PUSH
