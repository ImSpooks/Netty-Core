package me.ImSpooks.nettycore.server.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Blacklist {

    // Setting values
    private boolean enabled = false;
    private List<String> ips = new ArrayList<>();

    // Getters
    /**
     * @return Check for if blacklist is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return List of blacklist IPs
     */
    public List<String> getIps() {
        return ips;
    }
}