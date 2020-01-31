package me.ImSpooks.rpg.multiplayer.packets.handle;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public enum PacketType {
    NETWORKING(0),
    OTHER(100),
    ;

    public final int START_ID;

    PacketType(int START_ID) {
        this.START_ID = START_ID;
    }
}