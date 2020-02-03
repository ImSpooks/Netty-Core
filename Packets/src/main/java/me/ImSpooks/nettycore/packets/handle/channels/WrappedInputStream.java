package me.ImSpooks.nettycore.packets.handle.channels;

import io.netty.buffer.ByteBuf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class WrappedInputStream extends WrappedStream {

	/**
	 * Wrapped input stream instance
	 *
	 * @param buffer Input data
	 */
	public WrappedInputStream(ByteBuf buffer) {
		super(buffer);
	}

	/**
	 * @param b Bytes to read
	 * @throws IOException When an IO error occurs
	 */
	public void read(byte[] b) throws IOException {
		this.buffer.readBytes(b);
	}

	/**
	 * @return Reads the next incoming byte
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public int read() throws IOException {
		return this.buffer.readByte();
	}

	/**
	 * @return Reads the next incoming boolean
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public boolean readBoolean() throws IOException {
		return this.buffer.readBoolean();
	}

	/**
	 * @return Reads the next incoming double
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public double readDouble() throws IOException {
		return this.buffer.readDouble();
	}

	/**
	 * @return Reads the next incoming float
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public float readFloat() throws IOException {
		return this.buffer.readFloat();
	}

	/**
	 * @return Reads the next incoming int
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public int readInt() throws IOException {
		return this.buffer.readInt();
	}

	/**
	 * @return Reads the next incoming long
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public long readLong() throws IOException {
		return this.buffer.readLong();
	}

	/**
	 * @return  Reads the next incoming short
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public short readShort() throws IOException {
		return this.buffer.readShort();
	}

	/**
	 * @return Reads the next incoming string
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public String readString() throws IOException {
		int length = this.readInt();
		byte[] b = new byte[length];
		this.read(b);
		return new String(b);
	}

	/**
	 * @return Reads the next incoming UUID
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public UUID readUUID() throws IOException {
		long most = this.readLong();
		long least = this.readLong();
		return new UUID(most, least);
	}

	/**
	 * @return Reads the next incoming file with bytes
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
	public File readFile() throws IOException {
		File file = new File("tmp" + File.separator + this.readString());
		if (!file.exists()) file.createNewFile();

		try (FileOutputStream fos = new FileOutputStream(file)) {
			byte[] buffer = new byte[this.readInt()];
			this.read(buffer);
			fos.write(buffer);
		}

		return file;
	}

	/**
	 * @return Reads the next incoming object
	 * @throws IOException When an IO error occurs
	 * @throws ClassCastException When the next byte doesn't match the return type
	 */
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
