package me.ImSpooks.nettycore.client.networking;

import me.ImSpooks.nettycore.packets.handle.Packet;

/**
 * Created by Nick on 2 okt 2019.
 * Copyright © ImSpooks
 */
public abstract class IncomingListener<T> {

	private final long registerTime = System.currentTimeMillis();
	private final long expireAfterSeconds;

	/**
	 * @param expireAfterSeconds Seconds before packet expires
	 */
	public IncomingListener(long expireAfterSeconds) {
		this.expireAfterSeconds = expireAfterSeconds;
	}

	/**
	 * @return Seconds before packet expires
	 */
	public long getExpireAfterSeconds() {
		return expireAfterSeconds;
	}

	/**
	 * @return Register time in millis
	 * @see System#currentTimeMillis()
	 */
	public long getRegisterTime() {
		return registerTime;
	}

	/**
	 * @return {@code true} if the packet listener has been expired, {@code false} otherwise
	 */
	public boolean hasExpired() {
		long registeredSecondsAgo = (System.currentTimeMillis() - this.registerTime) / 1000L;
		return registeredSecondsAgo >= this.expireAfterSeconds;
	}

	/**
	 * Caller for receiver
	 */
	@SuppressWarnings("unchecked")
	boolean handle(Packet packet) {
		return this.onReceive((T) packet);
	}

	/**
	 * Return true to unregister, or false to keep listening
	 */
	protected abstract boolean onReceive(T packet);

	/**
	 * Called when the packet expires
	 */
	protected abstract void onExpire();

}
