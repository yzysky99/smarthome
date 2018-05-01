package com.smarthome.rtmp;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;

import com.smarthome.rtmp.utils.Constants;

/**
 * Created by stevyang on 17/10/7.
 */

public class AudioDataRecord {
    private static final String TAG = "AudioDataRecord";

    private AudioRecord mAudioRecord;
    private byte[] mAudioBuffer;
    private Thread AudioRecordThread;

    private AudioDataRecordCallback mAudioDataRecordCallback;
    private boolean isRecording;

    public interface AudioDataRecordCallback {
        void onRecvAudioData(byte[] data);
    }

    public void initAudioDevice(int sampleRate) {
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        int channelConfig = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        int buffsize = 2 * AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, sampleRate, channelConfig,
                audioFormat, buffsize);

        mAudioBuffer = new byte[Math.min(4096, buffsize)];
    }

    public void startAudioRecord() {
        AudioRecordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mAudioRecord.startRecording();
                while (isRecording && !Thread.interrupted()) {
                    int dataSize = mAudioRecord.read(mAudioBuffer, 0, mAudioBuffer.length);
                    if (dataSize > 0) {
                        byte[] audio = new byte[dataSize];
                        System.arraycopy(mAudioBuffer, 0, audio, 0, dataSize);
                        if (mAudioDataRecordCallback != null) {
                            mAudioDataRecordCallback.onRecvAudioData(audio);
                        }
                    }
                }
            }
        });

        isRecording = true;
        AudioRecordThread.start();
    }

    public void stopAudioRecord() {
        isRecording = false;
        AudioRecordThread.interrupt();
        Log.i(TAG, "stopAudioRecord!");

        if (mAudioRecord != null) {
            mAudioRecord.stop();
        }
    }

    public void release() {

        if (mAudioRecord != null) {
            mAudioRecord.release();
        }
    }

    public void setAudioDataRecordCallback(AudioDataRecordCallback audioDataRecordCallback) {
        this.mAudioDataRecordCallback = audioDataRecordCallback;
    }

}
