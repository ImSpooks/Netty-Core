package me.ImSpooks.nettycore.client.networking;

import me.ImSpooks.nettycore.packets.handle.Packet;

import java.io.IOException;
import java.util.*;

/**
 * Created by Nick on 9 Mar 2020.
 * Copyright © ImSpooks
 */
public class PacketReceiver {

	private final Map<Class<? extends Packet>, Set<IncomingListener<? extends Packet>>> listeners = new HashMap<>();
	private final Set<Runnable> establishRunners = new HashSet<>();

	/**
	 * Called when a packet was received
	 *
	 * @param packet Packet that was received
	 * @throws IOException
	 */
	public void received(Packet packet) throws IOException {
		synchronized (this.listeners) {
			Set<IncomingListener<? extends Packet>> registered = this.listeners.get(packet.getClass());
			if (registered == null || registered.isEmpty()) {
				return;
			}
			registered.removeIf(listener -> listener.handle(packet));
		}
	}

	/**
	 * Removes all the expired packets
	 */
	public void removeExpired() {
		synchronized (this.listeners) {
			for (Set<IncomingListener<? extends Packet>> set : this.listeners.values()) {
				Iterator<IncomingListener<? extends Packet>> it = set.iterator();
				while (it.hasNext()) {
					IncomingListener<? extends Packet> next = it.next();
					if (next.hasExpired()) {
						it.remove();
						next.onExpire();
					}
				}
			}
		}
	}

	/**
	 * Adds a packet listener
	 *
	 * @param packetType Packet class
	 * @param listener Incoming listener
	 * @param <T> Packet type
	 */
	public <T extends Packet> void addListener(Class<? extends T> packetType, IncomingListener<T> listener) {
		synchronized (this.listeners) {
			if (!this.listeners.containsKey(packetType)) {
				this.listeners.put(packetType, new HashSet<>());
			}
			this.listeners.get(packetType).add(listener);
		}
	}

	/**
	 * Add a callback that will be called when the connection has been established
	 */
	public void addEstablishRunnable(Runnable run) {
		synchronized (this.establishRunners) {
			this.establishRunners.add(run);
		}
	}

	/**
	 * Called when the connection has been established
	 */
	public void connectionEstablished() {
		synchronized (this.establishRunners) {
			for (Runnable r : this.establishRunners) {
				r.run();
			}
		}
	}
}
