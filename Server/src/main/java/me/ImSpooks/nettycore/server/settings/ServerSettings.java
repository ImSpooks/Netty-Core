package me.ImSpooks.nettycore.server.settings;

import me.ImSpooks.nettycore.common.Settings;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ServerSettings implements Settings {

    // Setting values
    private int port = 7000;
    private String password = "INSERT_PWD";
    private Whitelist whitelist = new Whitelist();
    private Blacklist blacklist = new Blacklist();

    // Getters
    /**
     * @return Server port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Server password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Server whitelist
     */
    public Whitelist getWhitelist() {
        return whitelist;
    }

    /**
     * @return Server blacklist
     */
    public Blacklist getBlacklist() {
        return blacklist;
    }
}