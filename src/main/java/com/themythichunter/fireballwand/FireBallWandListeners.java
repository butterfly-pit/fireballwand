package com.themythichunter.fireballwand;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Objects;

public class FireBallWandListeners implements Listener {
    private final FireballWand plugin;

    private final DecimalFormat df = new DecimalFormat("#0.00");

    final long WAND_COOLDOWN = 5000;
    final int WAND_MODEL_DATA = 1000727;

    private final HashMap<String, Long> wandCooldowns = new HashMap<>();

    public FireBallWandListeners(FireballWand plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void playerInteractEvent(PlayerInteractEvent event) {
        // Only allow left clicking air or entities.
        if(event.getAction() != Action.LEFT_CLICK_AIR && event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        Player player = event.getPlayer();
        assert event.getItem() != null;
        ItemStack heldItem = event.getItem();
        if(heldItem == null) {
            return;
        }
        ItemStack offHandItem = player.getInventory().getItemInOffHand();
        ItemMeta heldMeta = heldItem.getItemMeta();
        assert heldMeta != null;

        if(heldItem.getType() == Material.BLAZE_ROD && heldMeta.getCustomModelData() == WAND_MODEL_DATA && offHandItem.getType() == Material.FIRE_CHARGE) {
            // Check cooldown.
            if(wandCooldowns.containsKey(player.getName())) {
                double secondsLeft = (wandCooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000.0;

                if(secondsLeft > 0) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.RED + "You must wait " + df.format(secondsLeft) + " seconds before using this again!"));
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
                    return;
                }
            }

            // Create fireball.
            Fireball fireball = player.getWorld().spawn(player.getEyeLocation(), Fireball.class);
            fireball.setVelocity(player.getEyeLocation().getDirection().normalize().multiply(0.7));
            fireball.setYield(3.0f);
            fireball.setMetadata("wandfireball", new FixedMetadataValue(this.plugin, true));

            // Spawn particles at flying fireball.
            new BukkitRunnable() {
                @Override
                public void run() {
                    Location location = fireball.getLocation();
                    fireball.setVelocity(fireball.getDirection().normalize().multiply(0.7));
                    assert location.getWorld() != null;

                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);
                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);
                    location.getWorld().spawnParticle(Particle.FLAME, location, 1, 0.5, 0.5, 0.5, 0.01);

                    if(fireball.isDead()) {
                        cancel();
                    }
                }
            }.runTaskTimer(this.plugin, 1, 1);

            // Play sound.
            player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.0f, 1.0f);

            // Remove from inventory.
            offHandItem.setAmount(offHandItem.getAmount() - 1);

            // Set cooldown.
            this.wandCooldowns.put(player.getName(), System.currentTimeMillis() + this.WAND_COOLDOWN);

            // Remove glint and add it later.
            heldItem.removeEnchantment(Enchantment.DURABILITY);
            new BukkitRunnable() {
                @Override
                public void run() {
                    // Add glint later.
                    ItemStack wand;
                    for(ItemStack item : player.getInventory().getContents()) {
                        if(item != null && item.getType() == Material.BLAZE_ROD && Objects.requireNonNull(item.getItemMeta()).getCustomModelData() == WAND_MODEL_DATA) {
                            wand = item;
                            ItemMeta wandMeta = wand.getItemMeta();
                            assert wandMeta != null;

                            wandMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                            wandMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                            wand.setItemMeta(wandMeta);

                            break;
                        }
                    }

                    // Play sound.
                    // TODO: Play sound in off hand too.
                    if(player.getInventory().contains(Material.BLAZE_ROD)) {
                        for(ItemStack item : player.getInventory().getContents()) {
                            ItemMeta itemMeta = item.getItemMeta();
                            assert itemMeta != null;
                            if(item.getType() == Material.BLAZE_ROD && itemMeta.getCustomModelData() == WAND_MODEL_DATA) {
                                player.getWorld().playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.0f);
                                break;
                            }
                        }
                    }
                }
            }.runTaskLater(this.plugin, (this.WAND_COOLDOWN / 1000) * 20);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if(event.getEntity() instanceof Fireball && event.getEntity().hasMetadata("wandfireball")) {
            // Prevent block damage.
            event.setCancelled(true);

            // Play sound.
            World world = event.getLocation().getWorld();
            if(world != null) {
                world.playSound(event.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.0f);
            }

            // Create particles.
            event.getLocation().getWorld().spawnParticle(Particle.EXPLOSION_LARGE, event.getLocation(), 10, 0.5, 0.5, 0.5, 0.01);
        }
    }

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if(!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack itemStack = event.getItem().getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        assert itemMeta != null;

        if(itemMeta.hasCustomModelData() && itemMeta.getCustomModelData() == WAND_MODEL_DATA) {
            ItemMeta newMeta = itemStack.getItemMeta();

            if(wandCooldowns.containsKey(player.getName())) {
                double secondsLeft = (wandCooldowns.get(player.getName()) - System.currentTimeMillis()) / 1000.0;
                if(secondsLeft < 0) {
                    newMeta.addEnchant(Enchantment.DURABILITY, 1, true);
                    newMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
                    itemStack.setItemMeta(newMeta);
                }
                return;
            }
            newMeta.addEnchant(Enchantment.DURABILITY, 1, true);
            newMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            itemStack.setItemMeta(newMeta);
        }
    }
}
