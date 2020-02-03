package me.ImSpooks.nettycore.packets.handle.channels;

import io.netty.channel.Channel;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class WrappedChannel {

	// Channel
	private Channel channel;
	// Channel lock
	private final ReentrantLock channelLock = new ReentrantLock(true);

	/**
	 * Wrapped channel instance
	 *
	 * @param channel Channel to server
	 */
	public WrappedChannel(Channel channel) {
		this.channel = channel;
	}

	/**
	 * Lock the channel
	 */
	public void lock() {
		this.channelLock.lock();
	}

	/**
	 * Unlock the channel
	 */
	public void unlock() {
		this.channelLock.unlock();
	}

	/**
	 * @return Channel to server
	 */
	public Channel get() {
		return channel;
	}

	/**
	 * Set channel to server
	 *
	 * @param channel Channel to server
	 */
	public void set(Channel channel) {
		this.channel = channel;
	}
}
