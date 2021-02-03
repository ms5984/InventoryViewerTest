package com.github.ms5984.test.inventoryviewertest;

import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class InventoryUtil {

    private static final Map<String, Inventory> inventories = new HashMap<>();

    public static Inventory getInventoryByString(String string) {
        return inventories.computeIfAbsent(string, s -> {
            final Inventory inventory = Bukkit.createInventory(null, InventoryType.CHEST, "APrefix:" + s);
            System.out.println("Adding " + inventory + " to cache.");
            return inventory;
        });
    }

    public static void removeFromCache(Inventory inventory) {
        inventories.entrySet().stream()
                .filter(entry -> entry.getValue().equals(inventory))
                .map(Map.Entry::getKey)
//                .forEach(inventories::remove);
                .forEach(inv -> {
                    inventories.remove(inv);
                    System.out.println("Inventory removed from cache. " + inv);
                });
    }
}
