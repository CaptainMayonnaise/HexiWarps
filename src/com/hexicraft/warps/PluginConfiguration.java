package com.hexicraft.warps;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public class PluginConfiguration {
    private JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;

    PluginConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        configFile = new File(plugin.getDataFolder(), "config.yml");
        config = new YamlConfiguration();
        this.loadConfiguration();
    }

    public void loadConfiguration() {
        try {
            plugin.getLogger().info("Loading configuration file...");
            if (!configFile.exists()) {
                createConfiguration();
            }
            config.load(configFile);
        } catch (InvalidConfigurationException | IOException e) {
            plugin.getLogger().warning("Error loading configuration file.\n" + e.getMessage());
        }
    }

    public void createConfiguration() throws IOException {
        plugin.getLogger().info("File not found, creating new file...");
        config.set("MySQL.address", "localhost");
        config.set("MySQL.port", "3306");
        config.set("MySQL.database", "Hexicraft");
        config.set("MySQL.user", "root");
        config.set("MySQL.pass", "SMELLY");
        config.save(configFile);
    }

    public int getInt(String path) {
        return config.getInt(path);
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
}