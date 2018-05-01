package com.android.smarthome.image;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

public class ImageProtocolEncoder extends ProtocolEncoderAdapter {
	private final String charset;
	public ImageProtocolEncoder(String charset) {
		this.charset = charset;
	}

	public void encode(IoSession session, Object message,
					   ProtocolEncoderOutput out) throws Exception {
		System.out.println("encode: " + message);

	}
}
