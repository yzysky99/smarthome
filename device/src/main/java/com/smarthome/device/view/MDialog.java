package com.smarthome.device.view;

import android.app.Dialog;
import android.content.Context;

public class MDialog extends Dialog {
	private static final String TAG = "MDialog";
	private Context context;

	public MDialog(Context context) {
		super(context);
		this.context=context;
	}


}
