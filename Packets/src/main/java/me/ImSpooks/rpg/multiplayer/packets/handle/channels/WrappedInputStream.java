package me.ImSpooks.rpg.multiplayer.packets.handle.channels;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class WrappedInputStream extends WrappedStream {

	public WrappedInputStream(ByteBuf buffer) {
		super(buffer);
	}

	public void read(byte[] b) throws IOException {
		this.buffer.readBytes(b);
	}

	public int read() throws IOException {
		return this.buffer.readByte();
	}

	public boolean readBoolean() throws IOException {
		return this.buffer.readBoolean();
	}

	public double readDouble() throws IOException {
		return this.buffer.readDouble();
	}

	public float readFloat() throws IOException {
		return this.buffer.readFloat();
	}

	public int readInt() throws IOException {
		return this.buffer.readInt();
	}

	public long readLong() throws IOException {
		return this.buffer.readLong();
	}

	public short readShort() throws IOException {
		return this.buffer.readShort();
	}

	public String readString() throws IOException {
		int length = this.readInt();
		byte[] b = new byte[length];
		this.read(b);
		return new String(b);
	}

	public UUID readUUID() throws IOException {
		long most = this.readLong();
		long least = this.readLong();
		return new UUID(most, least);
	}

	public File readFile() throws IOException {
		return new File("");
	}

	public Object readTypePrefixed() throws IOException {
		int id = this.read();

		switch (id) {
			default:
			case -1:
				return null;
			case 0:
				return this.readString();
			case 1:
				return this.readInt();
			case 2:
				return this.readLong();
			case 3:
				return this.readDouble();
			case 4:
				return this.readBoolean();
			case 5:
				byte[] arr = new byte[this.readInt()];
				this.buffer.readBytes(arr);
				return arr;
			case 6:
				return this.readUUID();
			case 7:
				gson.fromJson(this.readString(), ArrayList.class);
			case 8:
				gson.fromJson(this.readString(), HashMap.class);
		}
		return null;
	}
}
