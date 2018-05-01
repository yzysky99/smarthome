package com.android.smarthome.image;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

public class ImageProtocolCodecFactory implements ProtocolCodecFactory {
    private final ImageProtocolEncoder encoder;
    private final ImageProtocolDecoder decoder;

    public ImageProtocolCodecFactory(String charset) {
        encoder = new ImageProtocolEncoder(charset);
        decoder = new ImageProtocolDecoder(charset);
    }

    public ProtocolEncoder getEncoder(IoSession session) {
        return encoder;
    }
    public ProtocolDecoder getDecoder(IoSession session) {
        return decoder;
    }

}