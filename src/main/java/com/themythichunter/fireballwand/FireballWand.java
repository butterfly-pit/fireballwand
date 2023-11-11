package com.themythichunter.fireballwand;

import org.bukkit.plugin.java.JavaPlugin;

public final class FireballWand extends JavaPlugin {
    private final ConfigHandler configHandler = new ConfigHandler(this);

    @Override
    public void onEnable() {
        configHandler.loadConfig();

        getServer().getPluginManager().registerEvents(new FireBallWandListeners(this), this);
        getLogger().info("FireballWand has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FireballWand has been disabled!");
    }
}
