package me.ImSpooks.nettycore.packets.handle;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public enum PacketType {
    /**
     * Networking
     */
    NETWORKING(0),
    ;

    public final int START_ID;

    /**
     * @param START_ID Start id for packets
     * @code START_ID + PACKET_ID
     */
    PacketType(int START_ID) {
        this.START_ID = START_ID;
    }
}