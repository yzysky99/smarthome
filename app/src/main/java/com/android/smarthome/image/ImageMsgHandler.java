package com.android.smarthome.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.Log;
import android.view.SurfaceHolder;

import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;

public class ImageMsgHandler extends IoHandlerAdapter {
    private static final String TAG = "ImageMsgHandler";

    SurfaceHolder mHolder;
    private int w;
    private int h;

    public ImageMsgHandler(SurfaceHolder holder, int w, int h) {
        this.mHolder = holder;
        this.w = w;
        this.h = h;
    }

    private void drawData(InputStream inputstream) {
        try {
            Canvas canvas;
            Bitmap bmp;
            bmp = BitmapFactory.decodeStream(inputstream);
            canvas = mHolder.lockCanvas();
            canvas.drawColor(Color.WHITE);
            RectF rectf = new RectF(0, 0, w, h);
            canvas.drawBitmap(bmp, null, rectf, null);
            mHolder.unlockCanvasAndPost(canvas);
        } catch (Exception ex) {

        } finally {

        }
    }

    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
        Log.d(TAG,"messageReceived");
        ImageMessage imageMessage = (ImageMessage) message;
        Log.d(TAG,"AllLength：" + imageMessage.getAllLength());
        Log.d(TAG,"imageName:" + imageMessage.getImageName());
        Log.d(TAG,"ImageLength：" + imageMessage.getImageLength());

        InputStream inputStream = new ByteArrayInputStream(imageMessage.getImage());
        drawData(inputStream);
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        Log.d(TAG,"sessionOpened!");
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception {
        super.messageSent(session, message);
    }

    @Override
    public void exceptionCaught(IoSession session, Throwable cause)
            throws Exception {
        super.exceptionCaught(session, cause);
        Log.d(TAG, "exceptionCaught");
    }
}
