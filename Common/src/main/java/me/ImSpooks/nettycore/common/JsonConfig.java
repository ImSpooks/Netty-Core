package me.ImSpooks.nettycore.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import org.tinylog.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Nick on 29 jan. 2020.
 * Copyright © ImSpooks
 */
public class JsonConfig {


    private static final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private String path;
    private File file;
    private LinkedHashMap<String, Object> object;

    /**
     * Initialized a json config file
     *
     * @param path Path to file
     * @param file Name of file
     */
    public JsonConfig(String path, String file) throws IOException {
        this(new File(path, file));
    }

    /**
     * Initialized a json config file
     *
     * @param file Name of file
     */
    public JsonConfig(String file) throws IOException {
        this(new File(file));
    }

    /**
     * Initialized a json config file
     *
     * @param file File
     */
    public JsonConfig(File file) throws IOException {
        this.file = file;
        this.path = this.file.getAbsoluteFile().getParent();

        this.initialize();
    }

    /**
     * Initializes the json config
     */
    @SuppressWarnings("unchecked")
    private void initialize() throws IOException {
            if (!Files.exists(this.file.toPath())) {
                Files.createDirectories(this.file.toPath().getParent());
                Files.createFile(this.file.toPath());

                this.object = new LinkedHashMap<>();
                this.save();
            }
            else {
                this.object = gson.fromJson(new String(Files.readAllBytes(this.file.toPath())), LinkedHashMap.class);
            }
    }

    /**
     * Reloads the config
     */
    public void reload() throws IOException {
        this.initialize();
    }

    /**
     * Saves the config
     */
    public void save() {
        try (Writer writer = new FileWriter(this.file, false)){
            writer.write(this.toString());
        } catch (IOException e) {
            Logger.error(e);
        }
    }

    /**
     * Assign a value to a key that doesn't exist
     * @see JsonConfig#set(String, Object) 
     *
     * @param key Name of key
     * @param defVal Value to set
     */
    public void expect(String key, Object defVal) {
        Object val = this.object.get(key);
        if (val == null) {
            this.set(key, defVal);
            this.save();
        }
    }

    /**
     * Set a value with a specified key
     *
     * @param key Name of key
     * @param val Value to set
     */
    @SuppressWarnings("unchecked")
    public void set(String key, Object val) {
        Map<String, Object> objects = this.object;
        
        if (key.contains(".")) {
            String[] split = key.split("\\.");
            for (int i = 0; i < split.length; i++) {
                String s =  split[i];
                if (i == split.length - 1) {
                    objects.put(s, val);
                }
                else {
                    if (!objects.containsKey(s))
                        objects.put(s, new LinkedTreeMap<>());
                    objects = (LinkedTreeMap<String, Object>) objects.get(s);
                }
            }
        }
        else {
            objects.put(key, val);
        }

    }

    /**
     * Get a value in the config
     *
     * @param key Name of key
     * @return Value from config
     * @throws IllegalArgumentException if key not found
     */
    public Object get(String key) {
        check(key);
        return this.object.get(key);
    }

    /**
     * Get a value in the config
     *
     * @param key Name of key
     * @param clazz Class to cast to
     * @return Value from config
     * @throws IllegalArgumentException if key not found
     */
    public <T> T get(String key, Class<T> clazz) {
        check(key);
        return gson.fromJson(gson.toJson(this.object.get(key)), clazz);
    }

    /**
     * Get a string value in the config
     *
     * @param key Name of key
     * @return String value from config
     * @throws IllegalArgumentException if key not found
     */
    public String getString(String key) {
        check(key);
        return (String) this.object.get(key);
    }

    /**
     * Get an integer value in the config
     *
     * @param key Name of key
     * @return Integer value from config
     * @throws IllegalArgumentException if key not found
     */
    public int getInt(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Integer) {
            return (Integer) val;
        }
        return ((Long) val).intValue();
    }
    /**
     * Get a byte value in the config
     *
     * @param key Name of key
     * @return Byte value from config
     * @throws IllegalArgumentException if key not found
     */
    public byte getByte(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Byte) {
            return (Byte) val;
        }
        return ((Long) val).byteValue();
    }
    /**
     * Get a short value in the config
     *
     * @param key Name of key
     * @return Short value from config
     * @throws IllegalArgumentException if key not found
     */
    public short getShort(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Short) {
            return (Short) val;
        }
        return ((Long) val).shortValue();
    }
    /**
     * Get an long value in the config
     *
     * @param key Name of key
     * @return Long value from config
     * @throws IllegalArgumentException if key not found
     */
    public long getLong(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Long) {
            return (Long) val;
        }
        throw new NumberFormatException("Not a number.");
    }

    /**
     * Get a double value in the config
     *
     * @param key Name of key
     * @return Double value from config
     * @throws IllegalArgumentException if key not found
     */
    public float getFloat(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Float) {
            return (Float) val;
        }
        return ((Long) val).floatValue();
    }

    /**
     * Get a double value in the config
     *
     * @param key Name of key
     * @return Double value from config
     * @throws IllegalArgumentException if key not found
     */
    public double getDouble(String key) {
        check(key);
        Object val = this.object.get(key);
        if (val instanceof Double) {
            return (Double) val;
        }
        return ((Long) val).doubleValue();
    }

    /**
     * Get a boolean value in the config
     *
     * @param key Name of key
     * @return Boolean value from config
     * @throws IllegalArgumentException if key not found
     */
    public boolean getBoolean(String key) {
        check(key);
        Object val = this.object.get(key);
        return val instanceof Boolean && (Boolean) val;
    }

    /**
     * Get a map in the config
     * @see JsonConfig#get(String, Class)
     *
     * @param key Name of key
     * @param map Class to cast to
     * @return Value from config
     * @throws IllegalArgumentException if key not found
     */
    public <T extends Map<?, ?>> T getMap(String key, Class<T> map) {
        return this.get(key, map);
    }

    /**
     * Get a list in the config
     * @see JsonConfig#get(String, Class)
     *
     * @param key Name of key
     * @param collection Class to cast to
     * @return Value from config
     * @throws IllegalArgumentException if key not found
     */
    public <T extends Collection<?>> T getCollection(String key, Class<T> collection) {
        check(key);
        return this.get(key, collection);
    }

    /**
     * Checks if the config contains a key
     * @param key Name of key
     * @throws IllegalArgumentException if key not found
     */
    private void check(String key) {
        if (!this.object.containsKey(key)) {
            throw new IllegalArgumentException("Key " + key + " was not found");
        }
    }

    /**
     * Returns the json string
     *
     * @return Json string
     */
    @Override
    public String toString() {
        return gson.toJson(this.object);
    }

    /**
     * Returns the directory of the file
     *
     * @return Parent path
     */
    public String getPath() {
        return path;
    }

    /**
     * Return the file of the config
     *
     * @return Config File
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the object were all the values are stored
     *
     * @return Hashmap with all values
     */
    public LinkedHashMap<String, Object> getObject() {
        return object;
    }
}