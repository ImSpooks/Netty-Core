package me.ImSpooks.nettycore.server.settings;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Whitelist {

    // Setting values
    private boolean enabled = false;
    private List<String> ips = new ArrayList<>();

    // Getters
    /**
     * @return Check for if whitelist is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @return List of whitelisted IPs
     */
    public List<String> getIps() {
        return ips;
    }
}