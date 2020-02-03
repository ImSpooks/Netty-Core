package me.ImSpooks.nettycore.packets.handle.channels;

import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public abstract class WrappedStream {

    // Gson instance
    protected final static Gson gson = new Gson();

    protected final ByteBuf buffer;

    public WrappedStream(ByteBuf buffer) {
        this.buffer = buffer;
    }

    /**
     * @return Byte buffer instance
     */
    public ByteBuf getBuffer() {
        return buffer;
    }
}