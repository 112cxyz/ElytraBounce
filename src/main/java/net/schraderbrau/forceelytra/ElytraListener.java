package net.schraderbrau.forceelytra;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ElytraListener implements Listener {

    private final ForceElytra plugin;

    public ElytraListener(ForceElytra plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (plugin.isForceElytraEnabled()) {
            if (!player.isGliding()) {
                player.setGliding(true);
            }
            if (!hasBoundElytra(player)) {
                giveBoundElytra(player);
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.isForceElytraEnabled()) {
            giveBoundElytra(player);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (plugin.isForceElytraEnabled()) {
            player.setGliding(false);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (plugin.isForceElytraEnabled() && event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            ItemStack currentItem = event.getCurrentItem();

            if (currentItem != null && currentItem.getType() == Material.ELYTRA && hasBoundElytra(player)) {
                event.setCancelled(true);
                player.sendMessage("You cannot remove the bound Elytra.");
            }
        }
    }

    private void giveBoundElytra(Player player) {
        ItemStack elytra = new ItemStack(Material.ELYTRA);
        ItemMeta meta = elytra.getItemMeta();
        meta.addEnchant(Enchantment.BINDING_CURSE, 1, true);
        meta.setUnbreakable(true);
        elytra.setItemMeta(meta);

        player.getInventory().setChestplate(elytra);
    }

    private boolean hasBoundElytra(Player player) {
        ItemStack chestplate = player.getInventory().getChestplate();
        if (chestplate == null || chestplate.getType() != Material.ELYTRA) {
            return false;
        }
        ItemMeta meta = chestplate.getItemMeta();
        return meta.hasEnchant(Enchantment.BINDING_CURSE) && meta.isUnbreakable();
    }
}
