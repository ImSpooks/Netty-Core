package me.ImSpooks.rpg.multiplayer.server.settings;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public class Whitelist {

    @Expose
    public boolean enabled = false;

    @Expose
    public List<String> ips = new ArrayList<>();

}