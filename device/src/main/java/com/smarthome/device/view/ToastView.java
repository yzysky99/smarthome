package com.smarthome.device.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.smarthome.device.R;

import java.util.Timer;
import java.util.TimerTask;

public class ToastView {
	
	public static Toast mToast;
	private int time;
	private Timer mTimer;
	
	public ToastView(Context context, String text) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast_view, null);
		TextView content = (TextView) view.findViewById(R.id.toast_content);
		content.setText(text);
		if(mToast != null) {
			mToast.cancel();
		}
		mToast = new Toast(context);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setView(view);
	}
	
	public ToastView(Context context, int text) {
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast_view, null);
		TextView content = (TextView) view.findViewById(R.id.toast_content);
		content.setText(text);
		if(mToast != null) {
			mToast.cancel();
		}
		mToast = new Toast(context);
		mToast.setDuration(Toast.LENGTH_SHORT);
		mToast.setView(view);
	}
	
	public void setGravity(int gravity, int xOffset, int yOffset) {
		mToast.setGravity(gravity, xOffset, yOffset);
	}
	
	public void setDuration(int duration) {
		mToast.setDuration(duration);
	}
	
	public void setLongTime(int duration) {
		time = duration;
		mTimer = new Timer();
		mTimer.schedule(new TimerTask(){
        	@Override
        	public void run() {
        		if(time-1000 >= 0) {
        			show();
        			time= time - 1000;
        		} else {
					mTimer.cancel();
        		}
        	}
        }, 0, 1000);
	}
	
	public void show() {
		mToast.show();
	}
	
	public static void cancel() {
		if(mToast != null) {
			mToast.cancel();
		}
	}

}
