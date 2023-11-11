package com.themythichunter.fireballwand;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {
    private final FireballWand plugin;

    public ConfigHandler(FireballWand plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        if(!this.plugin.getDataFolder().exists()) {
            if(this.plugin.getDataFolder().mkdir()) {
                this.plugin.getLogger().info("Created plugin folder.");
            }
        }

        File configFile = new File(this.plugin.getDataFolder(), "config.yml");

        if(!configFile.exists()) {
            try {
                if(configFile.createNewFile()) {
                    this.plugin.getConfig().options().copyDefaults(true);
                    this.plugin.saveConfig();

                    this.plugin.getLogger().info("Created a config file.");
                }
            }catch(IOException e) {
                this.plugin.getLogger().info("Error when creating the config file.");
            }
        }
    }
}
