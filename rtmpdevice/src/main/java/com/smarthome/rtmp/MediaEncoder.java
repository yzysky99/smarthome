package com.smarthome.rtmp;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.util.Log;

import com.smarthome.rtmp.utils.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;

import static android.media.MediaCodec.CONFIGURE_FLAG_ENCODE;
import static android.media.MediaFormat.KEY_BIT_RATE;
import static android.media.MediaFormat.KEY_COLOR_FORMAT;
import static android.media.MediaFormat.KEY_FRAME_RATE;
import static android.media.MediaFormat.KEY_I_FRAME_INTERVAL;
import static android.media.MediaFormat.KEY_MAX_INPUT_SIZE;
import static android.media.MediaFormat.KEY_ROTATION;

public class MediaEncoder {
    private static final String TAG = "MediaEncoder";

    private MediaCodec mVideoEncoder;
    private MediaCodec mAudioEncoder;

    private boolean videoEncoderStart, audioEncoderStart;
    private Thread videoEncoderThread, audioEncoderThread;
    private MediaEncoderCallback mMediaEncoderCallback;

    private long presentationTimeUs;
    private LinkedBlockingQueue<byte[]> videoQueue = new LinkedBlockingQueue<>();
    private LinkedBlockingQueue<byte[]> audioQueue = new LinkedBlockingQueue<>();

    public void setMediaEncoderCallback(MediaEncoderCallback mediaEncoderCallback) {
        this.mMediaEncoderCallback = mediaEncoderCallback;
    }

    public void start() {
        startAudioEncode();
        startVideoEncode();
    }

    public void stop() {
        stopAudioEncode();
        stopVideoEncode();
    }

    public void release() {
        releaseAudioEncoder();
        releaseVideoEncoder();
    }

    public void initAudioEncoder(int sampleRate, int chanelCount) throws IOException {
        mAudioEncoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC);
        MediaFormat format = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC,
                sampleRate, chanelCount);
        format.setInteger(KEY_MAX_INPUT_SIZE, 0);
        format.setInteger(KEY_BIT_RATE, sampleRate * chanelCount);
        mAudioEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
    }

    public int initVideoEncoder(int width, int height, int fps) throws IOException {
        MediaCodecInfo mediaCodecInfo = getMediaCodecInfoByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        int colorFormat = getColorFormat(mediaCodecInfo);
        mVideoEncoder = MediaCodec.createByCodecName(mediaCodecInfo.getName());
        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC,
                width, height);
        format.setInteger(KEY_MAX_INPUT_SIZE, 0);
        format.setInteger(KEY_BIT_RATE, Constants.BIT_RATE);
        format.setInteger(KEY_COLOR_FORMAT, colorFormat);
        format.setInteger(KEY_FRAME_RATE, fps);
        format.setInteger(KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(KEY_ROTATION, 90);
        mVideoEncoder.configure(format, null, null, CONFIGURE_FLAG_ENCODE);
        return colorFormat;
    }

    public void startVideoEncode() {
        if (mVideoEncoder == null) {
            throw new RuntimeException("请初始化视频编码器");
        }
        if (videoEncoderStart) {
            throw new RuntimeException("必须先停止");
        }

        videoEncoderThread = new Thread() {
            @Override
            public void run() {
                presentationTimeUs = System.currentTimeMillis() * 1000;
                mVideoEncoder.start();
                while (videoEncoderStart && !Thread.interrupted()) {
                    try {

                        byte[] data = videoQueue.take(); //待编码的数据
                        encodeVideoData(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }

            }
        };
        videoEncoderStart = true;
        videoEncoderThread.start();
    }

    public void startAudioEncode() {
        if (mAudioEncoder == null) {
            Log.e(TAG, "mAudioEncoder is null!");
            return;
        }

        if (audioEncoderStart) {
            Log.e(TAG, "audio encoder is start!");
            return;
        }

        audioEncoderThread = new Thread() {
            @Override
            public void run() {
                presentationTimeUs = System.currentTimeMillis() * 1000;
                mAudioEncoder.start();
                while (audioEncoderStart && !Thread.interrupted()) {
                    try {
                        byte[] data = audioQueue.take();
                        encodeAudioData(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }

            }
        };

        audioEncoderStart = true;
        audioEncoderThread.start();
    }

    public void putVideoData(byte[] data) {
        try {
            videoQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void putAudioData(byte[] data) {
        try {
            audioQueue.put(data);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stopVideoEncode() {
        videoEncoderStart = false;
        videoEncoderThread.interrupt();
        mVideoEncoder.stop();
    }

    public void stopAudioEncode() {
        audioEncoderStart = false;
        audioEncoderThread.interrupt();
        Log.i(TAG, "stopAudioEncode!");
        mAudioEncoder.stop();
    }

    public void releaseVideoEncoder() {
        mVideoEncoder.release();
    }

    public void releaseAudioEncoder() {
        mAudioEncoder.release();
    }

    private void encodeVideoData(byte[] data) {
        ByteBuffer[] inputBuffers = mVideoEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mVideoEncoder.getOutputBuffers();

        int inputBufferId = mVideoEncoder.dequeueInputBuffer(-1);
        if (inputBufferId >= 0) {
            ByteBuffer byteBuffer = inputBuffers[inputBufferId];
            byteBuffer.clear();
            byteBuffer.put(data, 0, data.length);
            long pts = new Date().getTime() * 1000 - presentationTimeUs;
            mVideoEncoder.queueInputBuffer(inputBufferId, 0, data.length, pts, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mVideoEncoder.dequeueOutputBuffer(bufferInfo, 0);
        if (outputBufferId >= 0) {
            ByteBuffer byteBuffer = outputBuffers[outputBufferId];
            if (null != mMediaEncoderCallback) {
                mMediaEncoderCallback.onRecvVideoData(byteBuffer, bufferInfo);
            }
            mVideoEncoder.releaseOutputBuffer(outputBufferId, false);
        }

    }

    private void encodeAudioData(byte[] data) {
        ByteBuffer[] inputBuffers = mAudioEncoder.getInputBuffers();
        ByteBuffer[] outputBuffers = mAudioEncoder.getOutputBuffers();
        int inputBufferId = mAudioEncoder.dequeueInputBuffer(-1);
        if (inputBufferId >= 0) {
            ByteBuffer byteBuffer = inputBuffers[inputBufferId];
            byteBuffer.clear();
            byteBuffer.put(data, 0, data.length);
            long pts = new Date().getTime() * 1000 - presentationTimeUs;
            mAudioEncoder.queueInputBuffer(inputBufferId, 0, data.length, pts, 0);
        }

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferId = mAudioEncoder.dequeueOutputBuffer(bufferInfo, 0);
        if (outputBufferId >= 0) {
            ByteBuffer byteBuffer = outputBuffers[outputBufferId];
            if (mMediaEncoderCallback != null) {
                mMediaEncoderCallback.onRecvAudioData(byteBuffer, bufferInfo);
            }
            mAudioEncoder.releaseOutputBuffer(outputBufferId, false);
        }

    }

    private static int getColorFormat(MediaCodecInfo mediaCodecInfo) {
        int matchedForamt = 0;
        MediaCodecInfo.CodecCapabilities codecCapabilities =
                mediaCodecInfo.getCapabilitiesForType(MediaFormat.MIMETYPE_VIDEO_AVC);
        for (int i = 0; i < codecCapabilities.colorFormats.length; i++) {
            int format = codecCapabilities.colorFormats[i];
            if (format >= codecCapabilities.COLOR_FormatYUV420Planar &&
                    format <= codecCapabilities.COLOR_FormatYUV420PackedSemiPlanar
                    ) {
                if (format >= matchedForamt) {
                    matchedForamt = format;
                }
            }
        }
        switch (matchedForamt) {
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar:
                Log.i(TAG, "selected yuv420p");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar:
                Log.i(TAG, "selected yuv420pp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar:
                Log.i(TAG, "selected yuv420sp");
                break;
            case MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar:
                Log.i(TAG, "selected yuv420psp");
                break;

        }
        return matchedForamt;
    }

    private static MediaCodecInfo getMediaCodecInfoByType(String mimeType) {
        for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (!codecInfo.isEncoder()) {
                continue;
            }
            String[] types = codecInfo.getSupportedTypes();
            for (int j = 0; j < types.length; j++) {
                if (types[j].equalsIgnoreCase(mimeType)) {
                    return codecInfo;
                }
            }
        }
        return null;
    }

    public interface MediaEncoderCallback {
        void onRecvVideoData(ByteBuffer byteBuffer, MediaCodec.BufferInfo info);
        void onRecvAudioData(ByteBuffer byteBuffer, MediaCodec.BufferInfo info);
    }
}
