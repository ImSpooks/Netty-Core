package me.ImSpooks.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;

/**
 * Created by Nick on 31 jan. 2020.
 * Copyright © ImSpooks
 */
public interface Settings {

    Gson gson = new GsonBuilder().setPrettyPrinting().excludeFieldsWithoutExposeAnnotation().disableHtmlEscaping().create();

    static <T> T load(File file, Class<T> clazz) throws FileNotFoundException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        return gson.fromJson(br, clazz);
    }

    static void save(File file, Settings settings) throws IOException {
        Writer writer = new FileWriter(file, false);
        writer.write(gson.toJson(settings));
    }
}