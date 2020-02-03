package me.ImSpooks.nettycore.packets.networking;

import io.netty.channel.Channel;
import me.ImSpooks.nettycore.packets.handle.Packet;
import me.ImSpooks.nettycore.packets.handle.channels.WrappedChannel;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class PacketFlusher {

    // Variables
    private final CoreImplementation core;
    private final LinkedList<Packet> waitingPackets = new LinkedList<>();

    /**
     * Packet flusher instance
     *
     * @param core Client instance
     */
    public PacketFlusher(CoreImplementation core) {
        this.core = core;
    }

    /**
     * Send packets to the server
     *
     * @param packets Packets to send
     */
    public void sendPacket(Packet... packets) {
        synchronized (this.waitingPackets) {
            for (Packet packet : packets) {
                this.waitingPackets.offer(packet);
            }
        }

        this.flushAll();
    }

    /**
     * Flushes all packets
     */
    public void flushAll() {
        WrappedChannel wrapped = this.core.getChannelUnsafe();

        List<Packet> processedBatch = new ArrayList<>();

        wrapped.lock();
        try {
            Channel ch = wrapped.get();
            if (ch == null)
                return;

            synchronized (this.waitingPackets) {
                if (this.waitingPackets.isEmpty())
                    return;

                Packet packet;
                while ((packet = this.waitingPackets.poll()) != null) {
                    processedBatch.add(packet);
                    ch.write(packet);
                }
            }
            ch.flush();
        } finally {
            wrapped.unlock();
        }

        wrapped.lock();
        try {
            Channel secondCheck = wrapped.get();

            // failed
            if (secondCheck == null) {
                Logger.error("Batch of {} packets failed... Trying again next cycle.", processedBatch.size());

                // reverse and add back
                Collections.reverse(processedBatch);
                for (Packet failed : processedBatch) {
                    this.waitingPackets.offerFirst(failed);
                }
                this.flushAll();
            }
        } finally {
            wrapped.unlock();
        }
    }

    /**
     * Send packets with a higher priority
     *
     * @param packets Packets to send
     */
    public void sendPacketHighPriority(Packet... packets) {
        synchronized (this.waitingPackets) {
            for (Packet packet : packets) {
                this.waitingPackets.offerFirst(packet);
            }
        }
        this.flushAll();
    }
}