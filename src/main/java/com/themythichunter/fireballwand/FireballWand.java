package com.themythichunter.fireballwand;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public final class FireballWand extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("FireballWand has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("FireballWand has been disabled!");
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        // Only allow left clicking air or entities.
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack heldItem = Objects.requireNonNull(event.getItem());
        ItemStack offHandItem = player.getInventory().getItemInOffHand();

        if(heldItem.getType() == Material.BLAZE_ROD && heldItem.getItemMeta().getCustomModelData() == 1000727 && offHandItem.getType() == Material.FIRE_CHARGE) {
            // Create fireball.
            Fireball fireball = player.getWorld().spawn(player.getEyeLocation(), Fireball.class);
            fireball.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.7));
            fireball.setShooter(player);
            fireball.setYield(3.0f);
            fireball.setMetadata("wandfireball", new FixedMetadataValue(this, true));

            new BukkitRunnable() {
                int tick = 0;

                @Override
                public void run() {
                    Location location = fireball.getLocation();
                    fireball.setVelocity(fireball.getDirection().normalize().multiply(0.7));

                    tick++;

                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);
                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);
                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);

                    if(fireball.isDead()) {
                        cancel();
                    }
                }
            }.runTaskTimer(this, 1, 1);

            // Play sound.
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

            // Remove from inventory.
            offHandItem.setAmount(offHandItem.getAmount() - 1);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.getEntity() instanceof Fireball && event.getEntity().hasMetadata("wandfireball")) {
            // Prevent block damage.
            event.setCancelled(true);

            // Play sound.
            event.getLocation().getWorld().playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.0f);

            // Create particles.
            event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, event.getLocation(), 10, 0.5, 0.5, 0.5, 0.01);
        }
    }
}
