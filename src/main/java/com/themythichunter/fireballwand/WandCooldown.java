package com.themythichunter.fireballwand;

public class WandCooldown {
    private final long cooldown;
    private long lastUsed;

    public WandCooldown(long cooldown) {
        this.cooldown = cooldown;
        this.lastUsed = 0;
    }

    public boolean isReady() {
        return System.currentTimeMillis() - this.lastUsed >= this.cooldown;
    }

    public void use() {
        this.lastUsed = System.currentTimeMillis();
    }
}
