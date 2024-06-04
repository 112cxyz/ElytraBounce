package net.schraderbrau.forceelytra;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class ForceElytra extends JavaPlugin {

    private boolean forceElytraEnabled = true;
    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    @Override
    public void onEnable() {
        getLogger().info("ForceElytra has been enabled!");

        // Register events
        getServer().getPluginManager().registerEvents(new ElytraListener(this), this);

        // Schedule task to ensure players are always in Elytra mode
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if (forceElytraEnabled) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() == org.bukkit.GameMode.SURVIVAL) {
                        player.setGliding(true);
                        giveElytra(player);
                    }
                }
            }
        }, 0L, 20L); // Runs every second (20 ticks)
    }

    @Override
    public void onDisable() {
        getLogger().info("ForceElytra has been disabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("forceelytra")) {
            if (args.length != 1) {
                sender.sendMessage("Usage: /forceelytra <enable|disable>");
                return false;
            }

            if (!sender.hasPermission("schrader.elytra.admin")) {
                sender.sendMessage("You don't have permission to use this command.");
                return false;
            }

            if (args[0].equalsIgnoreCase("enable")) {
                forceElytraEnabled = true;
                sender.sendMessage("ForceElytra has been enabled.");
            } else if (args[0].equalsIgnoreCase("disable")) {
                forceElytraEnabled = false;
                sender.sendMessage("ForceElytra has been disabled.");
            } else {
                sender.sendMessage("Usage: /forceelytra <enable|disable>");
            }
            return true;
        }
        return false;
    }

    public boolean isForceElytraEnabled() {
        return forceElytraEnabled;
    }

    public void giveElytra(Player player) {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        if (meta != null) {
            meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
            meta.setUnbreakable(true);
            elytra.setItemMeta(meta);
        }
        player.getInventory().setChestplate(elytra);
    }
    // Method to give the boost item
    public void giveBoostItem(Player player) {
        ItemStack boostItem = new ItemStack(Material.FEATHER);
        ItemMeta meta = boostItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Boost Feather");

            // Add lore
            List<String> lore = new ArrayList<>();
            lore.add("Press Right Click to Boost (45 Sec Cooldown)");
            meta.setLore(lore);

            boostItem.setItemMeta(meta);
            player.getInventory().setItem(0, boostItem); // Place boost item in slot 1
        }
    }

    // Method to handle boost item usage
    public void useBoostItem(Player player) {
        UUID playerId = player.getUniqueId();
        long cooldownTime = 45 * 1000; // 45 seconds cooldown

        if (!isOnCooldown(playerId)) {
            // Add cooldown
            setCooldown(playerId, cooldownTime);

            // Apply boost effect
            // Replace this with your desired boost effect
            player.setVelocity(player.getLocation().getDirection().multiply(2)); // Example boost effect
        } else {
            player.sendMessage("Boost is on cooldown. Please wait.");
        }
    }

    public boolean isBoostItem(ItemStack item) {
        if (item == null || item.getType() != Material.FEATHER || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName() || !meta.hasLore()) {
            return false;
        }

        String displayName = meta.getDisplayName();
        List<String> lore = meta.getLore();

        return "Boost Feather".equals(displayName) && lore != null && lore.contains("Press Right Click to Boost (45 Sec Cooldown)");
    }

    public boolean hasBoostItem(Player player) {
        ItemStack boostItem = new ItemStack(Material.FEATHER);
        ItemMeta meta = boostItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("Boost Feather");
            List<String> lore = new ArrayList<>();
            lore.add("Press Right Click to Boost (45 Sec Cooldown)");
            meta.setLore(lore);
            boostItem.setItemMeta(meta);
        }

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.isSimilar(boostItem)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOnCooldown(UUID playerId) {
        if (!cooldowns.containsKey(playerId)) {
            return false;
        }
        long lastUsed = cooldowns.get(playerId);
        return (System.currentTimeMillis() - lastUsed) < 10000;
    }

    public void setCooldown(UUID playerId, long cooldown) {
        cooldowns.put(playerId, System.currentTimeMillis());

        BossBar bossBar = bossBars.computeIfAbsent(playerId, k -> {
            BossBar bar = Bukkit.createBossBar("Boost Cooldown", BarColor.RED, BarStyle.SOLID);
            bar.setVisible(true);
            return bar;
        });

        bossBar.setProgress(1.0);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            bossBar.addPlayer(player);
        }

        Bukkit.getScheduler().runTaskTimer(this, task -> {
            long elapsed = System.currentTimeMillis() - cooldowns.get(playerId);
            double progress = 1.0 - (elapsed / (double) cooldown);
            if (progress <= 0) {
                task.cancel();
                bossBar.removeAll();
            } else {
                bossBar.setProgress(progress);
            }
        }, 0L, 20L);
    }
}
