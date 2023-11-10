package com.themythichunter.fireballwand;

import org.bukkit.plugin.java.JavaPlugin;

public final class FireballWand extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new FireBallWandListeners(this), this);
        getLogger().info("FireballWand has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FireballWand has been disabled!");
    }
}
