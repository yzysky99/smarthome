package com.android.smarthome.image;


import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;


public class ImageProtocolDecoder extends CumulativeProtocolDecoder {

	private final String charset;
	public ImageProtocolDecoder(String charset) {
		this.charset = charset;
	}

	protected boolean doDecode(IoSession session, IoBuffer in,
							   ProtocolDecoderOutput out) throws Exception {
		System.out.println("doDecode...");
		CharsetDecoder decoder = Charset.forName(charset).newDecoder();
		int msgLength = 0;
		int position = in.position();
		System.out.println("limit: "+in.limit());
		int remaining = in.remaining();
		System.out.println("remaining: "+remaining);
		try {
			if (remaining < 4) {
				in.position(position);
				return false;
			}

			msgLength = in.getInt();
			System.out.println(msgLength);
			if (remaining < msgLength || msgLength < 0) {
				in.position(position);
				return false;
			}
			ImageMessage imageMessage = new ImageMessage();
			int allLength = in.getInt(0);
			System.out.println("allLength:" + allLength);
			imageMessage.setAllLength(allLength);

			int nameLength = in.getInt();
			System.out.println("nameLength:"+ nameLength);
			imageMessage.setImageNameLength(nameLength);

			String name = in.getString(nameLength, decoder);
			System.out.println("name: " + name);
			imageMessage.setImageName(name);

			long imageLength = in.getLong();
			System.out.println("imageLength: " + imageLength);
			imageMessage.setImageLength(imageLength);

			byte[] image=new byte[(int) imageLength];
			in.get(image);
			imageMessage.setImage(image);

			out.write(imageMessage);
		} catch (Exception e) {
			in.position(position);
			return false;
		}
		return true;
	}


}
