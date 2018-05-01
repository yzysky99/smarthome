package com.smarthome.rtmp;

import android.app.Activity;
import android.media.MediaCodec;
import android.util.Log;
import android.view.SurfaceHolder;

import com.smarthome.rtmp.utils.Constants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class RtmpPush {
    private static final String TAG = "RtmpPush";

    public static final int NAL_SLICE = 1;
    public static final int NAL_SLICE_DPA = 2;
    public static final int NAL_SLICE_DPB = 3;
    public static final int NAL_SLICE_DPC = 4;
    public static final int NAL_SLICE_IDR = 5;
    public static final int NAL_SEI = 6;
    public static final int NAL_SPS = 7;
    public static final int NAL_PPS = 8;
    public static final int NAL_AUD = 9;
    public static final int NAL_FILLER = 12;

    private LinkedBlockingQueue<Runnable> mRunnables = new LinkedBlockingQueue<>();
    private Thread workThread;

    private AudioDataRecord mAudioDataRecord;
    private VideoDataRecord mVideoDataRecord;

    private MediaEncoder mMediaEncoder;

    private RtmpJni mRtmpJni;
    public boolean isPublish;

    private boolean loop;

    private int mSampleRate;
    private int mChannelCount;

    public void init() {

        mSampleRate = Constants.SAMPLE_RATE;
        mChannelCount = 2;

        mVideoDataRecord = new VideoDataRecord();
        mAudioDataRecord = new AudioDataRecord();
        mMediaEncoder = new MediaEncoder();
        mRtmpJni = new RtmpJni();
        setDataCallbackListener();

        workThread = new Thread("push_thread") {
            @Override
            public void run() {
                while (loop && !Thread.interrupted()) {
                    try {
                        Runnable runnable = mRunnables.take();
                        runnable.run();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        loop = true;
        workThread.start();
    }

    /**
     * 初始化音频采集器
     */
    public void initAudioDevice() {
        mAudioDataRecord.initAudioDevice(mSampleRate);
    }

    /**
     * 初始化摄像头
     *
     * @param act
     * @param holder
     */
    public void initVideoDevice(Activity act, SurfaceHolder holder) {
        mVideoDataRecord.initCamera(act, holder);
    }

    /**
     * 开始采集
     */
    public void startRecordAudioData() {
        mAudioDataRecord.startAudioRecord();
    }

    /**
     * 初始化编码器
     */
    public void initEncoders() {
        try {
            if (mMediaEncoder != null) {
                mMediaEncoder.initAudioEncoder(mSampleRate, mChannelCount);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            int colorFormat = mMediaEncoder.initVideoEncoder(mVideoDataRecord.getPreviewWidth(),
                    mVideoDataRecord.getPreviewHeight(), Constants.RTMP_FPS);
            mVideoDataRecord.setColorFormat(colorFormat);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 开始编码
     */
    public void startEncoder() {
        mMediaEncoder.start();
    }

    /**
     * 发布
     */
    public void starPublish() {
        if (isPublish) {
            return;
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int ret = mRtmpJni.initRtmp(Constants.RTMP_URL,
                        mVideoDataRecord.getPreviewWidth(),
                        mVideoDataRecord.getPreviewHeight(), Constants.RTMP_TIMEOUT);
                if (ret < 0) {
                    Log.e(TAG, "init rtmp failed!");
                    return;
                }

                isPublish = true;
            }
        };
        mRunnables.add(runnable);
    }


    /**
     * 停止发布
     */
    public void stopPublish() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mRtmpJni.stopRtmp();
                loop = false;
                workThread.interrupt();
            }
        };

        mRunnables.add(runnable);
    }

    /**
     * 停止编码
     */
    public void stopEncoder() {
        mMediaEncoder.stop();
    }

    /**
     * 停止采集
     */
    public void stopGather() {
        mAudioDataRecord.stopAudioRecord();
    }

    /**
     * 释放
     */
    public void release() {
        Log.i(TAG, "release!");
        mMediaEncoder.release();
        mVideoDataRecord.release();
        mAudioDataRecord.release();

        loop = false;
        if (workThread != null) {
            workThread.interrupt();
        }
    }

    private void setDataCallbackListener() {
        mVideoDataRecord.setVideoRecordCallback(new VideoDataRecord.VideoDataRecordCallback() {
            @Override
            public void onRecvVideoData(byte[] data, int colorFormat) {
                if (isPublish) {
                    mMediaEncoder.putVideoData(data);
                }
            }
        });

        mAudioDataRecord.setAudioDataRecordCallback(new AudioDataRecord.AudioDataRecordCallback() {
            @Override
            public void onRecvAudioData(byte[] data) {
                if (isPublish) {
                    mMediaEncoder.putAudioData(data);
                }
            }
        });

        mMediaEncoder.setMediaEncoderCallback(new MediaEncoder.MediaEncoderCallback() {
            @Override
            public void onRecvVideoData(ByteBuffer byteBuffer, MediaCodec.BufferInfo info) {
                onEncodedAvcFrame(byteBuffer, info);
            }

            @Override
            public void onRecvAudioData(ByteBuffer byteBuffer, MediaCodec.BufferInfo info) {
                onEncodeAacFrame(byteBuffer, info);
            }
        });
    }

    private void onEncodedAvcFrame(ByteBuffer byteBuffer, final MediaCodec.BufferInfo bufferInfo) {
        int offset = 4;
        //判断帧的类型
        if (byteBuffer.get(2) == 0x01) {
            offset = 3;
        }
        int type = byteBuffer.get(offset) & 0x1f;
        Log.d(TAG, "type=" + type);
        if (type == NAL_SPS) {
            //[0, 0, 0, 1, 103, 66, -64, 13, -38, 5, -126, 90, 1, -31, 16, -115, 64, 0, 0, 0, 1, 104, -50, 6, -30]
            //打印发现这里将 SPS帧和 PPS帧合在了一起发送
            // SPS为 [4，len-8]
            // PPS为后4个字节
            final byte[] pps = new byte[4];
            final byte[] sps = new byte[bufferInfo.size - 12];
            byteBuffer.getInt();// 抛弃 0,0,0,1
            byteBuffer.get(sps, 0, sps.length);
            byteBuffer.getInt();
            byteBuffer.get(pps, 0, pps.length);
            Log.d(TAG, "解析得到 sps:" + Arrays.toString(sps) + ",PPS=" + Arrays.toString(pps));
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mRtmpJni.pushSpsPpsData(sps, sps.length, pps, pps.length,
                            bufferInfo.presentationTimeUs / 1000);
                }
            };
            try {
                mRunnables.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else if (type == NAL_SLICE || type == NAL_SLICE_IDR) {
            final byte[] bytes = new byte[bufferInfo.size];
            byteBuffer.get(bytes);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mRtmpJni.pushVideoData(bytes, bytes.length,
                            bufferInfo.presentationTimeUs / 1000);
                }
            };
            try {
                mRunnables.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }

    private void onEncodeAacFrame(ByteBuffer byteBuffer, final MediaCodec.BufferInfo bufferInfo) {
        final byte[] bytes = new byte[bufferInfo.size];
        byteBuffer.get(bytes);

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (bufferInfo.size == 2) {
                    mRtmpJni.pushAudioSpec(bytes, 2);
                } else {
                    mRtmpJni.pushAudioData(bytes, bytes.length, bufferInfo.presentationTimeUs / 1000);
                }
            }
        };
        try {
            mRunnables.put(runnable);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
    private void onEncodeAacFrame(ByteBuffer bb, final MediaCodec.BufferInfo bufferInfo) {
        if (bufferInfo.size == 2) {
            // 我打印发现，这里应该已经是吧关键帧计算好了，所以我们直接发送
            final byte[] bytes = new byte[2];
            bb.get(bytes);
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mRtmpJni.pushAudioSpec(bytes, 2);
                }
            };
            try {
                mRunnables.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } else {
            final byte[] bytes = new byte[bufferInfo.size];
            bb.get(bytes);

            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    mRtmpJni.pushAudioData(bytes, bytes.length, bufferInfo.presentationTimeUs / 1000);
                }
            };
            try {
                mRunnables.put(runnable);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }

    }*/


}
