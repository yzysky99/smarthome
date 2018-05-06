package com.smarthome.rtmp;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodecInfo;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import static android.hardware.Camera.Parameters.FOCUS_MODE_AUTO;
import static android.hardware.Camera.Parameters.PREVIEW_FPS_MAX_INDEX;
import static android.hardware.Camera.Parameters.PREVIEW_FPS_MIN_INDEX;

import com.smarthome.rtmp.utils.Constants;

public class VideoDataRecord {
    private static final String TAG = "VideoDataRecord";

    private Camera mCamera;
    private Camera.Size previewSize;

    private Thread VideoRecordThread;
    private VideoDataRecordCallback mVideoDataRecordCallback;

    private boolean isRecording;

    private int mColorFormat;

    private LinkedBlockingQueue<byte[]> mVideoDataQueue = new LinkedBlockingQueue<>();

    interface VideoDataRecordCallback {
        void onRecvVideoData(byte[] data, int colorFormat);
    }

    public void setVideoRecordCallback(VideoDataRecordCallback videoDataRecordCallback) {
        this.mVideoDataRecordCallback = videoDataRecordCallback;
    }

    private void openCamera() {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,"open camera failed!");
            }
        }
    }

    public void release() {
        if (VideoRecordThread != null) {
            VideoRecordThread.interrupt();
            isRecording = false;
        }

        if (mCamera != null) {
            mCamera.setPreviewCallbackWithBuffer(null);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }

    }

    public void initCamera(Activity activity, SurfaceHolder holder) {
        release();
        openCamera();
        setCameraParameters();
        setCameraDisplayOrientation(activity, Camera.CameraInfo.CAMERA_FACING_BACK, mCamera);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mCamera.setPreviewCallbackWithBuffer(getPreviewCallback());
        mCamera.addCallbackBuffer(new byte[calculateFrameSize(ImageFormat.NV21)]);
        mCamera.startPreview();

        initVideoRecordThread();
    }

    public void setColorFormat(int colorFormat) {
        this.mColorFormat = colorFormat;
    }

    public int getPreviewWidth() {
        if (previewSize != null) {
            return previewSize.width;
        }
        return 1280;
    }

    public int getPreviewHeight() {
        if (previewSize != null) {
            return previewSize.height;
        }
        return 720;
    }

    private void initVideoRecordThread() {
        VideoRecordThread = new Thread() {
            private long preTime;
            byte[] dstByte = new byte[calculateFrameSize(ImageFormat.NV21)];

            @Override
            public void run() {
                while (isRecording && !Thread.interrupted()) {
                    try {

                        byte[] videoData = mVideoDataQueue.take();

                        Log.d(TAG,"colorFormat: " + mColorFormat);
                        Log.d(TAG,"width: " + previewSize.width);
                        Log.d(TAG,"height: " + previewSize.height);

                        if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
                            Yuv420Util.Nv21ToYuv420SP(videoData, dstByte, previewSize.width, previewSize.height);
                        } else if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar) {
                            Yuv420Util.Nv21ToI420(videoData, dstByte, previewSize.width, previewSize.height);
                        } else if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible) {
                            // Yuv420_888
                        } else if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedPlanar) {
                            // Yuv420packedPlannar 和 yuv420sp很像
                            // 区别在于 加入 width = 4的话 y1,y2,y3 ,y4公用 u1v1
                            // 而 yuv420dp 则是 y1y2y5y6 共用 u1v1
                            //这样处理的话颜色核能会有些失真。
                            Yuv420Util.Nv21ToYuv420SP(videoData, dstByte, previewSize.width, previewSize.height);
                        } else if (mColorFormat == MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar) {

                        } else {
                            System.arraycopy(videoData, 0, dstByte, 0, videoData.length);
                        }

                        if (mVideoDataRecordCallback != null) {
                            mVideoDataRecordCallback.onRecvVideoData(dstByte, mColorFormat);
                        }
                        //处理完成之后调用 addCallbackBuffer()
                        if (preTime != 0) {
                            // 延时
                            int shouldDelay = (int) (1000.0 / Constants.RTMP_FPS);
                            int realDelay = (int) (System.currentTimeMillis() - preTime);
                            int delta = shouldDelay - realDelay;
                            if (delta > 0) {
                                sleep(delta);
                            }
                        }

                        mCamera.addCallbackBuffer(videoData);
                        preTime = System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
        };

        isRecording = true;
        VideoRecordThread.start();
    }

    public Camera.PreviewCallback getPreviewCallback() {
        return new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(final byte[] data, final Camera camera) {
                if (data != null) {
                    try {
                        mVideoDataQueue.put(data);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    camera.addCallbackBuffer(new byte[calculateFrameSize(ImageFormat.NV21)]);
                }
            }
        };
    }

    private int calculateFrameSize(int format) {
        return previewSize.width * previewSize.height * ImageFormat.getBitsPerPixel(format) / 8;
    }

    private void setNewCameraParameters() {
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Collections.sort(previewSizeList, new Comparator<Camera.Size>() {

            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                if (lhs.height == rhs.height) {
                    return lhs.width == rhs.width ? 0 : (lhs.width > rhs.width ? 1 : -1);
                } else if (lhs.height > rhs.height) {
                    return 1;
                }
                return -1;
            }
        });

        int previewWidth = 1280;
        int previewHeight = 720;

        int diff = Integer.MAX_VALUE;
        for (int i = 0; i < previewSizeList.size(); i++) {
            Camera.Size size = previewSizeList.get(i);

            if ((size.width % 16 == 0) && (size.height % 16 == 0)) {
                Log.d(TAG," 11 stev width: " + size.width + " height: " + size.height);
                int currentDiff = Math.abs(size.height - previewHeight);
                if (currentDiff < diff) {
                    diff = currentDiff;
                    previewSize = size;
                }
            }
        }
    }

    public static void setAutoFocusMode(Camera camera) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.size() > 0 && focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                camera.setParameters(parameters);
            } else if (focusModes.size() > 0) {
                parameters.setFocusMode(focusModes.get(0));
                camera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setCameraParameters() {
        setAutoFocusMode(mCamera);
        setNewCameraParameters();
        Camera.Parameters parameters = mCamera.getParameters();
        List<Camera.Size> supportedPreviewSizes = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : supportedPreviewSizes) {
            if (size.width >= Constants.RTMP_MIN_WIDTH && size.width <=  Constants.RTMP_MIN_WIDTH) {
                previewSize = size;
                Log.i(TAG, String.format("find preview size width=%d,height=%d", previewSize.width,
                        previewSize.height));
                break;
            }
        }

        int[] destRange = {Constants.RTMP_FPS * 1000, Constants.RTMP_FPS * 1000};
        List<int[]> supportedPreviewFpsRange = parameters.getSupportedPreviewFpsRange();
        for (int[] range : supportedPreviewFpsRange) {
            if (range[PREVIEW_FPS_MAX_INDEX] >= Constants.RTMP_FPS * 1000) {
                destRange = range;
                Log.d(TAG, String.format("find fps range :%s", Arrays.toString(destRange)));
                break;
            }
        }

        if (previewSize == null) {
            throw new RuntimeException("find previewSize error");
        }

        if (parameters.getSupportedFocusModes().contains(
                Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        Log.d(TAG," stev width: " + previewSize.width + " height: " + previewSize.height);
        parameters.setPreviewSize(previewSize.width, previewSize.height);
        parameters.setPreviewFpsRange(destRange[PREVIEW_FPS_MIN_INDEX],
                destRange[PREVIEW_FPS_MAX_INDEX]);
        parameters.setRecordingHint(true);

        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        for (int i = 0; null != supportedFocusModes && i < supportedFocusModes.size(); i++) {
            if (FOCUS_MODE_AUTO.equals(supportedFocusModes.get(i))) {
                parameters.setFocusMode(FOCUS_MODE_AUTO);
                break;
            }
        }

        if (parameters.isVideoStabilizationSupported()) {
            parameters.setVideoStabilization(true);
        }

        parameters.setPreviewFormat(ImageFormat.NV21);

        mCamera.setParameters(parameters);

        Camera.Size size = parameters.getPreviewSize();
        Log.d(TAG,"stev width: " + size.width + " height: " + size.height);
//        previewSize = size;
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

}
