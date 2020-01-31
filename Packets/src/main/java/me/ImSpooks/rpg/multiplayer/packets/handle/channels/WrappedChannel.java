package me.ImSpooks.rpg.multiplayer.packets.handle.channels;

import io.netty.channel.Channel;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class WrappedChannel {

	private Channel channel;
	private final ReentrantLock channelLock = new ReentrantLock(true);

	public WrappedChannel(Channel channel) {
		this.channel = channel;
	}

	public void lock() {
		this.channelLock.lock();
	}

	public void unlock() {
		this.channelLock.unlock();
	}

	public Channel get() {
		return channel;
	}

	public void set(Channel channel) {
		this.channel = channel;
	}
}
