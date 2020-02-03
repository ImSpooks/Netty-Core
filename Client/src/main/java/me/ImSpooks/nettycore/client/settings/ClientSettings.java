package me.ImSpooks.nettycore.client.settings;

import me.ImSpooks.nettycore.common.Settings;

/**
 * Created by Nick on 03 feb. 2020.
 * Copyright © ImSpooks
 */
public class ClientSettings implements Settings {

    // Settings values
    private String targetIp = "127.0.0.1";
    private int port = 7000;
    private String name = "INSERT_NAME";
    private String password = "INSERT_PWD";

    // Getters

    /**
     * @return Server IP
     */
    public String getTargetIp() {
        return targetIp;
    }

    /**
     * @return Server Port
     */
    public int getPort() {
        return port;
    }

    /**
     * @return Client Name
     */
    public String getName() {
        return name;
    }

    /**
     * @return Server Password
     */
    public String getPassword() {
        return password;
    }
}