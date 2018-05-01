#include "rtmp_interface.h"

int RtmpInterface::initRtmp(std::string url, int w, int h, int timeOut) {

    RTMP_LogSetLevel(RTMP_LOGDEBUG);
    rtmp = RTMP_Alloc();
    RTMP_Init(rtmp);
    LOGI("time out = %d",timeOut);
    rtmp->Link.timeout = timeOut;
    RTMP_SetupURL(rtmp, (char *) url.c_str());
    RTMP_EnableWrite(rtmp);

    if (!RTMP_Connect(rtmp, NULL) ) {
        LOGI("RTMP_Connect error");
        return -1;
    }
    LOGI("RTMP_Connect success.");

    if (!RTMP_ConnectStream(rtmp, 0)) {
        LOGI("RTMP_ConnectStream error");
        return -1;
    }

    uint32_t offset = 0;
    char buffer[512];
    char *output = buffer;
    char *outend = buffer + sizeof(buffer);
    char send_buffer[512];

    output = AMF_EncodeString(output, outend, &av_onMetaData);
    *output++ = AMF_ECMA_ARRAY;

    output = AMF_EncodeInt32(output, outend, 5);
    output = AMF_EncodeNamedNumber(output, outend, &av_width, w);
    output = AMF_EncodeNamedNumber(output, outend, &av_height, h);
    output = AMF_EncodeNamedNumber(output, outend, &av_duration, 0.0);
    output = AMF_EncodeNamedNumber(output, outend, &av_videocodecid, 7);
    output = AMF_EncodeNamedNumber(output, outend, &av_audiocodecid, 10);
    output = AMF_EncodeInt24(output, outend, AMF_OBJECT_END);

    int body_len = output - buffer;
    int output_len = body_len + FLV_TAG_HEAD_LEN + FLV_PRE_TAG_LEN;

    send_buffer[offset++] = 0x12; //tagtype scripte
    send_buffer[offset++] = (uint8_t) (body_len >> 16); //data len
    send_buffer[offset++] = (uint8_t) (body_len >> 8); //data len
    send_buffer[offset++] = (uint8_t) (body_len); //data len
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0; //time stamp
    send_buffer[offset++] = 0x00; //stream id 0
    send_buffer[offset++] = 0x00; //stream id 0
    send_buffer[offset++] = 0x00; //stream id 0

    memcpy(send_buffer + offset, buffer, body_len);

    return RTMP_Write(rtmp, send_buffer, output_len);
}

int RtmpInterface::pushSpsPpsData(BYTE *sps, int spsLen, BYTE *pps, int ppsLen, long timestamp) {

    int i;
    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + 1024);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    BYTE *body = (BYTE *) packet->m_body;

    i = 0;
    body[i++] = 0x17; //1:keyframe 7:AVC
    body[i++] = 0x00; // AVC sequence header

    body[i++] = 0x00;
    body[i++] = 0x00;
    body[i++] = 0x00; //fill in 0

    /*AVCDecoderConfigurationRecord*/
    body[i++] = 0x01;
    body[i++] = sps[1]; //AVCProfileIndecation
    body[i++] = sps[2]; //profile_compatibilty
    body[i++] = sps[3]; //AVCLevelIndication
    body[i++] = 0xff;//lengthSizeMinusOne

    /*SPS*/
    body[i++] = 0xe1;
    body[i++] = (spsLen >> 8) & 0xff;
    body[i++] = spsLen & 0xff;
    /*sps data*/
    memcpy(&body[i], sps, spsLen);

    i += spsLen;

    /*PPS*/
    body[i++] = 0x01;
    /*sps data length*/
    body[i++] = (ppsLen >> 8) & 0xff;
    body[i++] = ppsLen & 0xff;
    memcpy(&body[i], pps, ppsLen);
    i += ppsLen;

    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nBodySize = i;
    packet->m_nChannel = 0x04;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_MEDIUM;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    /*发送*/
    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);
    return 0;
}

int RtmpInterface::pushVideoData(BYTE *buf, int len, long timestamp) {
    int type;

    /*去掉帧界定符*/
    if (buf[2] == 0x00) {/*00 00 00 01*/
        buf += 4;
        len -= 4;
    } else if (buf[2] == 0x01) {
        buf += 3;
        len - 3;
    }

    type = buf[0] & 0x1f;

    RTMPPacket *packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 9);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    packet->m_nBodySize = len + 9;


    /* send video packet*/
    BYTE *body = (BYTE *) packet->m_body;
    memset(body, 0, len + 9);

    /*key frame*/
    body[0] = 0x27;
    if (type == NAL_SLICE_IDR) {
        body[0] = 0x17; //关键帧
    }

    body[1] = 0x01;/*nal unit*/
    body[2] = 0x00;
    body[3] = 0x00;
    body[4] = 0x00;

    body[5] = (len >> 24) & 0xff;
    body[6] = (len >> 16) & 0xff;
    body[7] = (len >> 8) & 0xff;
    body[8] = (len) & 0xff;

    /*copy data*/
    memcpy(&body[9], buf, len);

    packet->m_hasAbsTimestamp = 0;
    packet->m_packetType = RTMP_PACKET_TYPE_VIDEO;
    packet->m_nInfoField2 = rtmp->m_stream_id;
    packet->m_nChannel = 0x04;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nTimeStamp = timestamp;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);

    return 0;
}

int RtmpInterface::pushAudioSpec(BYTE *data, int spec_len) {
    RTMPPacket *packet;
    BYTE *body;
    int len = spec_len;//spec len 是2
    packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
    memset(packet, 0, RTMP_HEAD_SIZE);
    packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
    body = (BYTE *) packet->m_body;

    /*AF 00 +AAC RAW data*/
    body[0] = 0xAF;
    body[1] = 0x00;
    memcpy(&body[2], data, len);/*data 是AAC sequeuece header数据*/

    packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
    packet->m_nBodySize = len + 2;
    packet->m_nChannel = STREAM_CHANNEL_AUDIO;
    packet->m_nTimeStamp = 0;
    packet->m_hasAbsTimestamp = 0;
    packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
    packet->m_nInfoField2 = rtmp->m_stream_id;

    if (RTMP_IsConnected(rtmp)) {
        RTMP_SendPacket(rtmp, packet, TRUE);
    }
    free(packet);

    return 0;
}

int RtmpInterface::pushAudioData(BYTE *data, int len, long timeOffset) {
    if (len > 0) {
        RTMPPacket *packet;
        BYTE *body;
        packet = (RTMPPacket *) malloc(RTMP_HEAD_SIZE + len + 2);
        memset(packet, 0, RTMP_HEAD_SIZE);
        packet->m_body = (char *) packet + RTMP_HEAD_SIZE;
        body = (BYTE *) packet->m_body;

        /*AF 00 +AAC Raw data*/
        body[0] = 0xAF;
        body[1] = 0x01;
        memcpy(&body[2], data, len);

        packet->m_packetType = RTMP_PACKET_TYPE_AUDIO;
        packet->m_nBodySize = len + 2;
        packet->m_nChannel = STREAM_CHANNEL_AUDIO;
        packet->m_nTimeStamp = timeOffset;
        packet->m_hasAbsTimestamp = 0;
        packet->m_headerType = RTMP_PACKET_SIZE_LARGE;
        packet->m_nInfoField2 = rtmp->m_stream_id;
        if (RTMP_IsConnected(rtmp)) {
            RTMP_SendPacket(rtmp, packet, TRUE);
        }
        LOGD("send packet body[0]=%x,body[1]=%x", body[0], body[1]);
        free(packet);

    }
    return 0;
}

int RtmpInterface::stopRtmp() const {
    RTMP_Close(rtmp);
    RTMP_Free(rtmp);
    return 0;
}

RtmpInterface::~RtmpInterface() { stopRtmp(); }