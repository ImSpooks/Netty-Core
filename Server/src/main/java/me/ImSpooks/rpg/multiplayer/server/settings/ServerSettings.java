package me.ImSpooks.rpg.multiplayer.server.settings;

import com.google.gson.annotations.Expose;
import me.ImSpooks.core.Settings;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class ServerSettings implements Settings {

    @Expose
    public int port = 7000;

    @Expose
    public String password = "INSERT_PWD";

    @Expose
    public Whitelist whitelist;

    @Expose
    public Blacklist blacklist;


}