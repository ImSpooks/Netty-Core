package me.ImSpooks.nettycore.packets.enums;

public enum DisconnectReason {
    /**
     * Blacklisted
     */
    BLACKLISTED(0),

    /**
     * Not whitelisted
     */
    NOT_WHITELISTED(1),

    /**
     * Unknown
     */
    UNKNOWN(-1),
    ;

    public static DisconnectReason[] CACHE = values();

    private final int id;

    DisconnectReason(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static DisconnectReason getFromId(int id) {
        for (DisconnectReason disconnectReason : CACHE) {
            if (disconnectReason.getId() == id)
                return disconnectReason;
        }
        throw new IllegalArgumentException("No disconnect reason found with id " + id);
    }
}