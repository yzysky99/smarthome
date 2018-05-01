package com.android.smarthome.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.android.smarthome.R;
import com.android.smarthome.image.ImageMsgHandler;
import com.android.smarthome.image.ImageProtocolCodecFactory;

import org.apache.mina.core.service.IoConnector;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;

public class RemoteCameraActivity extends AppCompatActivity {
    private static final String TAG = "RemoteCameraActivity";

    private String serverIp = "";
    private int serverPort;

    private SurfaceHolder holder;
    private int width;
    private int height;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_camera);
        initData();
        initConnect();
    }

    private void initData() {
        serverIp = getIntent().getExtras().getString("remoteCameraIp");
        serverPort = getIntent().getExtras().getInt("remoteCameraPort");
        width = getWindowManager().getDefaultDisplay().getWidth();
        height = getWindowManager().getDefaultDisplay().getHeight();

        SurfaceView surface = (SurfaceView)findViewById(R.id.surface);
        surface.setKeepScreenOn(true);
        holder = surface.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                       int height) {
            }
        });
    }

    private void initConnect() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                IoConnector connector = new NioSocketConnector();
                connector.getFilterChain().addLast("code",new ProtocolCodecFilter(new ImageProtocolCodecFactory("utf-8")));
                connector.setConnectTimeoutMillis(30000);
                connector.setHandler(new ImageMsgHandler(holder, width, height));

                Log.d(TAG,"serverIp: " + serverIp);
                Log.d(TAG,"serverPort: " + serverPort);
                connector.connect(new InetSocketAddress(serverIp, serverPort));

//                String IP_ADDRESS = "192.168.0.101";
//                int PORT = 8068;
//                connector.connect(new InetSocketAddress(IP_ADDRESS, PORT));
                Log.d(TAG,"connect");
            }
        }).start();
    }
}
