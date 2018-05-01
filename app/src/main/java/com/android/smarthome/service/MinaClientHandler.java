package com.android.smarthome.service;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.json.JSONObject;

public class MinaClientHandler extends IoHandlerAdapter {
    private static final String TAG = "MinaClientHandler";

    MinaMsgResult mMinaMsgResult;
    public MinaClientHandler(MinaMsgResult minaMsgResult) {
        mMinaMsgResult = minaMsgResult;
    }

    @Override
    public void messageReceived(IoSession session, Object message) throws Exception {
        System.out.println(message.toString()+"=====");
        super.messageReceived(session, message);

        String msgStr = message.toString();
        JSONObject jsonObject = new JSONObject(msgStr);
        String msgType = jsonObject.getString("Type");
        String msgContent = jsonObject.getString("Content");
        if (msgType.equals("register")) {
            mMinaMsgResult.registerResult(msgContent);
        } else if (msgType.equals("login")) {
            mMinaMsgResult.loginResult(msgContent);
        }


    }
    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        System.out.println("====="+message.toString());
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause) throws Exception {

        super.exceptionCaught(session, cause);
    }


}
