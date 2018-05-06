package com.android.smarthome.network;

import android.util.Log;

import com.android.smarthome.utils.Constants;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;
import org.json.JSONObject;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import static org.apache.mina.filter.codec.textline.LineDelimiter.WINDOWS;

/**
 * Created by stevyang on 2017/9/18.
 */

public class TcpClient extends IoHandlerAdapter {
    private static final String TAG = "TcpClient";
    private IoConnector connector;
    private static IoSession session;
    private String sendData = "";
    IDataRecvListener mDataRecvListener;
    public interface IDataRecvListener {
        int onDataRecv(String data);
    }

    public TcpClient() {
        connector = new NioSocketConnector();

        connector.getFilterChain().addLast("codec",
                new ProtocolCodecFilter( new TextLineCodecFactory( Charset.forName("UTF-8"),
                                WINDOWS.getValue(), WINDOWS.getValue())));
        connector.setConnectTimeoutMillis(30000);
        connector.setHandler(this);
        ConnectFuture connFuture = connector.connect(new InetSocketAddress(Constants.IP_ADDRESS, Constants.PORT));

        connFuture.awaitUninterruptibly();
        session = connFuture.getSession();
    }

    public void sendData(String data, IDataRecvListener dataRecvListener) {
        mDataRecvListener = dataRecvListener;
        if (data != null && session != null) {
            Log.d(TAG,"send data: " + data);
            session.write(data);
        } else {
            Log.d(TAG, "send data error!");
        }
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {
        cause.printStackTrace();
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        String msg = message.toString();

        String msgStr = message.toString();
        JSONObject jsonObject = new JSONObject(msgStr);
        String msgType = jsonObject.getString("Type");
        String msgContent = jsonObject.getString("Content");
        if (msgType.equals("register")) {

        } else if (msgType.equals("login")) {
            mDataRecvListener.onDataRecv(msgContent);
        }
        Log.d(TAG, "messageReceived:" + msg);
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        System.out.println("Sent messages: " + message.toString());
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
//        session.write(sendData);
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        System.out.println("sessionClosed...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        System.out.println("sessionCreated...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) throws Exception {
        System.out.println("sessionIdle...");
    }


//    public static void main(String[] args) throws Exception {
//        TcpClient client = new TcpClient("data");
//        client.sendData("1234", new IDataRecvListener() {
//            @Override
//            public int onDataRecv(String data) {
//
//                return 0;
//            }
//        });
//        client.connector.dispose(true);
//    }
}
