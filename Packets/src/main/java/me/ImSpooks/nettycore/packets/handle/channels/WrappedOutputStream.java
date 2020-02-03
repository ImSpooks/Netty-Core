package me.ImSpooks.nettycore.packets.handle.channels;


import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class WrappedOutputStream extends WrappedStream {

    /**
     * Wrapped output stream instance
     * @param buffer Out buffer
     */
    public WrappedOutputStream(ByteBuf buffer) {
        super(buffer);
    }

    /**
     * @param data Byte array
     * @throws IOException when an IO error occurs
     */
    public void write(byte[] data) throws IOException {
        this.buffer.writeBytes(data);
    }

    /**
     * @param byt Byte
     * @throws IOException when an IO error occurs
     */
    public void write(int byt) throws IOException {
        this.buffer.writeByte(byt);
    }

    /**
     * @param b Boolean
     * @throws IOException when an IO error occurs
     */
    public void writeBoolean(boolean b) throws IOException {
        this.buffer.writeBoolean(b);
    }

    /**
     * @param d Double
     * @throws IOException when an IO error occurs
     */
    public void writeDouble(double d) throws IOException {
        this.buffer.writeDouble(d);
    }

    /**
     * @param f Float
     * @throws IOException when an IO error occurs
     */
    public void writeFloat(float f) throws IOException {
        this.buffer.writeFloat(f);
    }

    /**
     * @param i Int
     * @throws IOException when an IO error occurs
     */
    public void writeInt(int i) throws IOException {
        this.buffer.writeInt(i);
    }

    /**
     * @param l Long
     * @throws IOException when an IO error occurs
     */
    public void writeLong(long l) throws IOException {
        this.buffer.writeLong(l);
    }

    /**
     * @param s Short
     * @throws IOException when an IO error occurs
     */
    public void writeShort(short s) throws IOException {
        this.buffer.writeShort(s);
    }

    /**
     * @param s String
     * @throws IOException when an IO error occurs
     */
    public void writeString(String s) throws IOException {
        this.writeInt(s.length());
        this.write(s.getBytes());
    }

    /**
     * @param uuid UUID
     * @throws IOException when an IO error occurs
     */
    public void writeUUID(UUID uuid) throws IOException {
        this.writeLong(uuid.getMostSignificantBits());
        this.writeLong(uuid.getLeastSignificantBits());
    }

    /**
     * @param file File
     * @throws IOException when an IO error occurs
     */
    public void writeFile(File file) throws IOException {
        byte[] data = Files.readAllBytes(file.toPath());

        this.writeString(file.getName());
        this.writeInt(data.length);
        this.write(data);
    }

    /**
     * @param o Object
     * @throws IOException when an IO error occurs
     */
    public void writeTypePrefixed(Object o) throws IOException {
        if (o == null) {
            this.write(-1);
        }
        else if (o instanceof String) {
            this.write(0);
            this.writeString((String) o);
        }
        else if (o instanceof Integer) {
            this.write(1);
            this.writeInt((Integer) o);
        }
        else if (o instanceof Long) {
            this.write(2);
            this.writeLong((Long) o);
        }
        else if (o instanceof Double) {
            this.write(3);
            this.writeDouble((Double) o);
        }
        else if (o instanceof Boolean) {
            this.write(4);
            this.writeBoolean((Boolean) o);
        }
        else if (o instanceof byte[]) {
            this.write(5);
            this.writeInt(((byte[]) o).length);
            this.buffer.writeBytes((byte[]) o);
        }
        else if (o instanceof UUID) {
            this.write(6);
            this.writeUUID((UUID) o);
        }
        else if (o instanceof List) {
            this.write(7);
            this.writeString(gson.toJson(o));
        }
        else if (o instanceof Map) {
            this.write(8);
            this.writeString(gson.toJson(o));
        }
    }
}
