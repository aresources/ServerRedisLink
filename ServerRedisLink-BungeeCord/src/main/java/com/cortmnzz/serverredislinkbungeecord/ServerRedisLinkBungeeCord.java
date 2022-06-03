package com.cortmnzz.serverredislinkbungeecord;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPubSub;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;

public final class ServerRedisLinkBungeeCord extends Plugin {

    JedisPool jedisPool;

    @Override
    public void onEnable() {
        File configurationFile = new File(getDataFolder(), "config.yml");
        Configuration configuration;

        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                try (InputStream inputStream = getResourceAsStream("config.yml");
                     OutputStream outputStream = Files.newOutputStream(configFile.toPath())) {
                    ByteStreams.copy(inputStream, outputStream);
                }
            } catch (IOException exception) {
                throw new RuntimeException("Unable to create configuration file", exception);
            }
        }

        try {
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configurationFile);
        } catch (IOException exception) {
            throw new RuntimeException("Unable to load configuration file", exception);
        }

        jedisPool = new JedisPool(configuration.getString("redis.host"), configuration.getInt("redis.port"));

        if (configuration.getBoolean("redis.auth.enabled")) {
            if (configuration.getString("redis.username") != null) {
                jedisPool.getResource().auth(configuration.getString("redis.auth.username"), configuration.getString("redis.auth.password"));
            } else {
                jedisPool.getResource().auth(configuration.getString("redis.auth.password"));
            }
        }
        try {
            getProxy().getScheduler().runAsync(this, () -> jedisPool.getResource().subscribe(new JedisPubSub() {
                @Override
                public void onMessage(String channel, String message) {
                    if (message.equals("update")) {
                        updateServerList();
                    }
                }
            }, "server-link"));
        } catch (Exception exception) {
            throw new RuntimeException("Unable to connect with Redis server", exception);
        }

        updateServerList();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public void updateServerList() {
        getProxy().getServers().clear();

        for (String redisKey: jedisPool.getResource().keys("server-link:*")) {
            String serverName = redisKey.split(":")[1];
            String[] arguments = jedisPool.getResource().get(redisKey).split(":");

            ServerInfo serverInfo = ProxyServer.getInstance().constructServerInfo(serverName, new InetSocketAddress(arguments[0], Integer.parseInt(arguments[1])), "", false);
            getProxy().getServers().put(serverName, serverInfo);

            getLogger().info("Server {0} registered!".replace("{0}", serverName));
        }
    }
}
