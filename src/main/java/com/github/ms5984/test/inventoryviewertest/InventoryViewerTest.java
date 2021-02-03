package com.github.ms5984.test.inventoryviewertest;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class InventoryViewerTest extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            return false;
        }
        final Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("Specify test name");
            return true;
        }
        final String testName = String.join(" ", args);
        final Inventory inventoryByString = InventoryUtil.getInventoryByString(testName);
        final ItemStack listViewers = new ItemStack(Material.GREEN_CONCRETE);
        final ItemMeta meta1 = listViewers.getItemMeta();
        meta1.setDisplayName("List viewers");
        listViewers.setItemMeta(meta1);
        final ItemStack kickAll = new ItemStack(Material.RED_CONCRETE);
        final ItemMeta meta2 = listViewers.getItemMeta();
        meta2.setDisplayName("Kick all viewers");
        kickAll.setItemMeta(meta2);
        inventoryByString.setItem(12, listViewers);
        inventoryByString.setItem(14, kickAll);
        player.openInventory(inventoryByString);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length <= 1) {
            return Collections.singletonList("testname");
        }
        return Collections.emptyList();
    }

    @EventHandler
    public void onManagedInventoryClick(InventoryClickEvent e) {
        final Inventory clickedInventory = e.getClickedInventory();
        if (!e.getView().getTitle().startsWith("APrefix:") || clickedInventory == null) {
            return;
        }
        if (!clickedInventory.equals(e.getView().getTopInventory())) {
            return;
        }
        final int slot = e.getSlot();
        if (slot != 12 && slot != 14) {
            return;
        }
        e.setCancelled(true);
        new BukkitRunnable() {
            @Override
            public void run() {
                if (slot == 12) { // "list viewers"
                    e.getWhoClicked().sendMessage("Viewers: " +
                            clickedInventory.getViewers().stream()
                            .map(HumanEntity::getName)
                            .collect(Collectors.joining(", ")));
                } else if (slot == 14) { // "kick all viewers"
                    clickedInventory.getViewers().forEach(humanEntity -> {
                        humanEntity.closeInventory();
                        if (humanEntity == e.getWhoClicked()) {
                            humanEntity.sendMessage(ChatColor.AQUA + "Kicked all players");
                            return;
                        }
                        humanEntity.sendMessage(ChatColor.RED + "You were kicked!");
                    });
                }
            }
        }.runTask(this);
    }

    @EventHandler
    public void onManagedInventoryClose(InventoryCloseEvent e) {
        final Inventory inventory = e.getInventory();
        if (!e.getView().getTitle().startsWith("APrefix:")) {
            return;
        }
        if (inventory.getType() != InventoryType.CHEST) {
            return;
        }
        if (e.getViewers().stream().anyMatch(humanEntity -> !humanEntity.equals(e.getPlayer()))) {
            System.out.println("Removing from cache" + inventory);
            InventoryUtil.removeFromCache(inventory);
        }
    }
}
