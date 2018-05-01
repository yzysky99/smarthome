package com.android.smarthome.ui;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.smarthome.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class CameraSurfaceActivity extends AppCompatActivity {
    private static final String TAG = "CameraSurfaceActivity";
    private SurfaceHolder surfaceHolder;
//    private Thread videoGetThread;
    private Canvas canvas;
    URL videoUrl;
    private String cameraDataUrl;
    private int width;
    private int height;
    HttpURLConnection httpURLConnection;
    Bitmap bitmap;
    boolean isCameraRunning = false;

//    private EditText cmdEdit;
    private Button sendCameraCmdBtn;
    public static String sendCameraCmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_surface);
        initView();
        initData();
    }

    private void initData() {
//        sendCameraCmdBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        sendCameraCmd(sendCameraCmd);
//                    }
//                }).start();
//            }
//        });
    }

    private void initView() {
        cameraDataUrl = getIntent().getExtras().getString("CameraIp");
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

//        cmdEdit = (EditText) findViewById(R.id.camera_cmd);
//        sendCameraCmdBtn = (Button) findViewById(R.id.send_cmd);
//        sendCameraCmd = cmdEdit.getText().toString();

        SurfaceView surface = (SurfaceView)findViewById(R.id.camera_surface);
        surface.setKeepScreenOn(true);
        surfaceHolder = surface.getHolder();
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                new Thread(new VideoDataRunnable()).start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
                // TODO Auto-generated method stub
            }
        });
    }


    public String sendCameraCmd(String cameraCmd) {
        StringBuilder sb = new StringBuilder("aa");
        HttpURLConnection connection = null;
        try {
            URL url = new URL("http://192.168.0.102:8080/?action=command&command=" + cameraCmd);
            connection = (HttpURLConnection) url.openConnection();
            // 设置请求方式
            connection.setRequestMethod("GET");
//            connection.setConnectTimeout(10 * 1000);
            // 设置编码格式
            connection.setRequestProperty("Charset", "UTF-8");
            // 传递自定义参数
//            http://192.168.0.100:8080/?action=command&command=led_on
//            connection.setRequestProperty("action=", "command");
//            connection.setRequestProperty("command=", "led_off");
            // 设置容许输出
//            connection.setDoOutput(true);
            Log.d(TAG,"stev response code : " + connection.getResponseCode());
            // 获取返回数据
            if(connection.getResponseCode() == 200){
                Log.d(TAG,"get response code");
                InputStream is = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                sb = new StringBuilder();
                String line = null;
                try {
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                sb.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(connection!=null){
                connection.disconnect();
            }
        }

        Log.d(TAG,"sb: " + sb.toString());
        return sb.toString();
    }

    private void videoDataDraw() {
        try {
            InputStream inputstream = null;
//			url = "http://192.168.8.1:8083/?action=snapshot";
            videoUrl=new URL(cameraDataUrl);
            httpURLConnection = (HttpURLConnection)videoUrl.openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.connect();
            inputstream = httpURLConnection.getInputStream();
            bitmap = BitmapFactory.decodeStream(inputstream);

            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            RectF rectf = new RectF(0, 0, width, height);
            canvas.drawBitmap(bitmap, null, rectf, null);
            surfaceHolder.unlockCanvasAndPost(canvas);
            httpURLConnection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        } finally {

        }
    }

    public class VideoDataRunnable implements Runnable{
        @Override
        public void run() {
            while (true) {//isCameraRunning
                Log.d(TAG,"videoDataDraw");
                videoDataDraw();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isCameraRunning = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isCameraRunning = false;
    }
}
