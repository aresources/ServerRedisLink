package com.cortmnzz.serverredislink;

import org.bukkit.plugin.java.JavaPlugin;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public final class ServerRedisLink extends JavaPlugin {

    JedisPool jedisPool;
    String folderName = new File(System.getProperty("user.dir")).getName();
    String redisKey = String.join(":", Arrays.asList("server-link", folderName));

    @Override
    public void onEnable() {
        // Plugin startup logic
        jedisPool = new JedisPool(getConfig().getString("redis.host"), getConfig().getInt("redis.port"));

        if (getConfig().getBoolean("redis.auth")) {
            if (getConfig().getString("redis.username") != null) {
                jedisPool.getResource().auth(getConfig().getString("redis.auth.username"), getConfig().getString("redis.auth.password"));
            } else {
                jedisPool.getResource().auth(getConfig().getString("redis.auth.password"));
            }
        }

        saveDefaultConfig();
        reloadConfig();
        registerServer();
    }

    @Override
    public void onDisable() {
        jedisPool.getResource().del(redisKey);
        jedisPool.getResource().publish("server-link", "update");
        getLogger().info("Server successfully unregistered!");
    }
    public void registerServer() {
        List<String> arguments = Arrays.asList(getServer().getIp(), String.valueOf(getServer().getPort()));

        try {
            jedisPool.getResource().set(redisKey, String.join(":", arguments));

            getServer().getScheduler().runTaskAsynchronously(this, () -> jedisPool.getResource().publish("server-link", "update"));

            getLogger().info("Server successfully registered!");
        } catch (Exception exception) {
            throw new RuntimeException("Unable to connect with Redis server", exception);
        }
    }
}
