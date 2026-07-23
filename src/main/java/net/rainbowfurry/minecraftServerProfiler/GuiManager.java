package net.rainbowfurry.minecraftServerProfiler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;

public class GuiManager implements Listener {
    private final MinecraftServerProfiler plugin;
    private final ConfigManager configManager;
    private final ProfilerManager profilerManager;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public GuiManager(MinecraftServerProfiler plugin, ConfigManager configManager, ProfilerManager profilerManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.profilerManager = profilerManager;
    }

    public void openMainGui(Player player) {
        if (!configManager.isGuiEnabled()) {
            return;
        }
        Component title = miniMessage.deserialize(configManager.getGuiTitle());
        int size = configManager.getGuiSize();
        Inventory inventory = Bukkit.createInventory(null, size, title);

        fillGui(inventory, Material.GRAY_STAINED_GLASS_PANE);

        inventory.setItem(10, createSystemInfoItem());
        inventory.setItem(11, createGameInfoItem());
        inventory.setItem(12, createWorldsItem());
        inventory.setItem(13, createBreakdownItem());
        inventory.setItem(14, createTopEntitiesItem());
        inventory.setItem(15, createTopChunkItem());
        inventory.setItem(16, createTopPluginItem());

        player.openInventory(inventory);
    }

    private void fillGui(Inventory inventory, Material material) {
        ItemStack pane = new ItemStack(material);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize("<reset>"));
            pane.setItemMeta(meta);
        }
        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, pane);
            }
        }
    }

    private ItemStack createSystemInfoItem() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientSystem() + "<bold>System Info</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            double tps = profilerManager.getTPS();
            double mspt = profilerManager.getMSPT();
            double cpuUsage = profilerManager.getCpuUsage();
            long memoryUsed = profilerManager.getHeapMemoryUsage().getUsed();
            long memoryMax = profilerManager.getHeapMemoryUsage().getMax();
            double memoryPercent = (double) memoryUsed / memoryMax * 100;

            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "TPS: " + getStatusColor(tps, configManager.getTpsThresholdGood(), configManager.getTpsThresholdModerate(), configManager.getTpsThresholdBad()) + String.format("%.2f", tps) + "<reset>"));
            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "MSPT: " + getStatusColor(mspt, configManager.getMsptThresholdGood(), configManager.getMsptThresholdModerate(), configManager.getMsptThresholdBad(), true) + String.format("%.2f", mspt) + " ms<reset>"));
            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "CPU: " + (cpuUsage >= 0 ? configManager.getTextColorHighlight() + String.format("%.1f", cpuUsage) + "%" : configManager.getTextColorNone() + "N/A") + "<reset>"));
            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Memory: " + getStatusColor(memoryPercent, configManager.getMemoryThresholdGood(), configManager.getMemoryThresholdModerate(), configManager.getMemoryThresholdBad(), true) + ProfilerManager.formatBytes(memoryUsed) + " / " + ProfilerManager.formatBytes(memoryMax) + " (" + String.format("%.1f", memoryPercent) + "%)<reset>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createGameInfoItem() {
        ItemStack item = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientGame() + "<bold>Game Info</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            int players = profilerManager.getTotalPlayerCount();
            int tileEntities = profilerManager.getTotalTileEntities();
            int chunks = Bukkit.getWorlds().stream().mapToInt(w -> w.getLoadedChunks().length).sum();

            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Players: " + getStatusColor(players, configManager.getPlayersThresholdGood(), configManager.getPlayersThresholdModerate(), configManager.getPlayersThresholdBad()) + players + "<reset>"));
            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Tile Entities: " + getStatusColor(tileEntities, configManager.getTileEntitiesThresholdGood(), configManager.getTileEntitiesThresholdModerate(), configManager.getTileEntitiesThresholdBad()) + tileEntities + "<reset>"));
            lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Chunks: " + getStatusColor(chunks, configManager.getChunksThresholdGood(), configManager.getChunksThresholdModerate(), configManager.getChunksThresholdBad()) + chunks + "<reset>"));
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createWorldsItem() {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientWorlds() + "<bold>Worlds</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            Map<String, Integer> worldChunkCounts = profilerManager.getWorldChunkCounts();
            lore.add(miniMessage.deserialize(configManager.getTextColorNone() + "Klicke für Details"));
            lore.add(miniMessage.deserialize(""));
            for (Map.Entry<String, Integer> entry : worldChunkCounts.entrySet()) {
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + entry.getKey() + ": " + configManager.getTextColorHighlight() + entry.getValue() + " Chunks<reset>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createBreakdownItem() {
        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientBreakdown() + "<bold>Performance Breakdown</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            Map<String, Double> categories = profilerManager.calculateCategories();
            for (Map.Entry<String, Double> entry : categories.entrySet()) {
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + entry.getKey() + ": " + getStatusColor(entry.getValue(), configManager.getPercentageThresholdGood(), configManager.getPercentageThresholdModerate(), configManager.getPercentageThresholdBad(), true) + String.format("%.1f", entry.getValue()) + "%<reset>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTopEntitiesItem() {
        ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientTop() + "<bold>Top Entities</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            lore.add(miniMessage.deserialize(configManager.getTextColorNone() + "Klicke für Top 5"));
            Map.Entry<String, Long> topEntity = profilerManager.getTopEntity();
            if (topEntity != null) {
                lore.add(miniMessage.deserialize(""));
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Top: " + configManager.getTextColorHighlight() + topEntity.getKey() + " (" + topEntity.getValue() + ")<reset>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTopChunkItem() {
        ItemStack item = new ItemStack(Material.GRASS_BLOCK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientTop() + "<bold>Top Chunk</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            ProfilerManager.DetailedChunkInfo topChunk = profilerManager.getDetailedTopChunk();
            if (topChunk != null) {
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Welt: " + configManager.getTextColorHighlight() + topChunk.world.getName() + "<reset>"));
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Position: " + configManager.getTextColorHighlight() + topChunk.pos.x + ", " + topChunk.pos.z + "<reset>"));
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Gesamt Entities: " + configManager.getTextColorHighlight() + topChunk.totalEntities + "<reset>"));
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Tile Entities: " + configManager.getTextColorHighlight() + topChunk.tileEntityCount + "<reset>"));
                lore.add(miniMessage.deserialize(""));
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Entity-Typen:"));
                List<Map.Entry<String, Integer>> sortedEntities = topChunk.entityTypeCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(3)
                        .toList();
                for (Map.Entry<String, Integer> entry : sortedEntities) {
                    lore.add(miniMessage.deserialize("  " + configManager.getTextColorLabel() + entry.getKey() + ": " + configManager.getTextColorHighlight() + entry.getValue() + "<reset>"));
                }
                if (!topChunk.tileEntityTypeCounts.isEmpty()) {
                    lore.add(miniMessage.deserialize(""));
                    lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Tile-Entity-Typen:"));
                    List<Map.Entry<String, Integer>> sortedTileEntities = topChunk.tileEntityTypeCounts.entrySet().stream()
                            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                            .limit(3)
                            .toList();
                    for (Map.Entry<String, Integer> entry : sortedTileEntities) {
                        lore.add(miniMessage.deserialize("  " + configManager.getTextColorLabel() + entry.getKey() + ": " + configManager.getTextColorHighlight() + entry.getValue() + "<reset>"));
                    }
                }
            } else {
                lore.add(miniMessage.deserialize(configManager.getTextColorNone() + "Keine Chunks geladen<reset>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createTopPluginItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(miniMessage.deserialize(configManager.getGradientTop() + "<bold>Top Plugin</bold><reset>"));
            List<Component> lore = new ArrayList<>();
            Map.Entry<String, Double> topPlugin = profilerManager.getTopPlugin();
            if (topPlugin != null) {
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Top: " + configManager.getTextColorHighlight() + topPlugin.getKey() + " (" + String.format("%.2f", topPlugin.getValue()) + " ms<reset>"));
            } else {
                lore.add(miniMessage.deserialize(configManager.getTextColorNone() + "Keine Plugin-Daten<reset>"));
            }
            meta.lore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String getStatusColor(double value, double good, double moderate, double bad) {
        return getStatusColor(value, good, moderate, bad, false);
    }

    private String getStatusColor(double value, double good, double moderate, double bad, boolean inverted) {
        if (inverted) {
            if (value <= good) {
                return configManager.getStatusColorGood();
            } else if (value <= moderate) {
                return configManager.getStatusColorModerate();
            } else if (value <= bad) {
                return configManager.getStatusColorBad();
            } else {
                return configManager.getStatusColorCritical();
            }
        } else {
            if (value >= good) {
                return configManager.getStatusColorGood();
            } else if (value >= moderate) {
                return configManager.getStatusColorModerate();
            } else if (value >= bad) {
                return configManager.getStatusColorBad();
            } else {
                return configManager.getStatusColorCritical();
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        event.setCancelled(true);
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) {
            return;
        }
        Component title = event.getView().title();
        Component mainTitle = miniMessage.deserialize(configManager.getGuiTitle());
        Component worldsTitle = miniMessage.deserialize(configManager.getGradientWorlds() + "<bold>Weltdetails</bold><reset>");
        Component topEntitiesTitle = miniMessage.deserialize(configManager.getGradientTop() + "<bold>Top 5 Entities</bold><reset>");

        if (title.equals(mainTitle)) {
            int slot = event.getRawSlot();
            if (slot == 12) {
                openWorldsGui(player);
            } else if (slot == 14) {
                openTopEntitiesGui(player);
            }
        } else if (title.equals(worldsTitle) || title.equals(topEntitiesTitle)) {
            int slot = event.getRawSlot();
            if (slot == 49) {
                openMainGui(player);
            }
        }
    }

    private void openWorldsGui(Player player) {
        Component title = miniMessage.deserialize(configManager.getGradientWorlds() + "<bold>Weltdetails</bold><reset>");
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        fillGui(inventory, Material.GRAY_STAINED_GLASS_PANE);
        Map<String, Integer> worldChunkCounts = profilerManager.getWorldChunkCounts();
        int slot = 10;
        for (Map.Entry<String, Integer> entry : worldChunkCounts.entrySet()) {
            if (slot >= 53) break;
            ItemStack item = new ItemStack(Material.GRASS_BLOCK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize(configManager.getTextColorHighlight() + "<bold>" + entry.getKey() + "</bold><reset>"));
                List<Component> lore = new ArrayList<>();
                org.bukkit.World world = Bukkit.getWorld(entry.getKey());
                if (world != null) {
                    int entities = world.getEntities().size();
                    int tileEntities = 0;
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        tileEntities += chunk.getTileEntities().length;
                    }
                    lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Chunks: " + configManager.getTextColorHighlight() + entry.getValue() + "<reset>"));
                    lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Entities: " + configManager.getTextColorHighlight() + entities + "<reset>"));
                    lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Tile Entities: " + configManager.getTextColorHighlight() + tileEntities + "<reset>"));
                }
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(miniMessage.deserialize(configManager.getTextColorLabel() + "<bold>Zurück</bold><reset>"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);
        player.openInventory(inventory);
    }

    private void openTopEntitiesGui(Player player) {
        Component title = miniMessage.deserialize(configManager.getGradientTop() + "<bold>Top 5 Entities</bold><reset>");
        Inventory inventory = Bukkit.createInventory(null, 54, title);
        fillGui(inventory, Material.GRAY_STAINED_GLASS_PANE);
        Map<String, Long> entityCounts = new HashMap<>();
        for (org.bukkit.World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String name = entity.getType().name();
                entityCounts.put(name, entityCounts.getOrDefault(name, 0L) + 1);
            }
        }
        List<Map.Entry<String, Long>> sortedEntities = entityCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .toList();
        int slot = 10;
        for (Map.Entry<String, Long> entry : sortedEntities) {
            if (slot >= 53) break;
            ItemStack item = new ItemStack(Material.ZOMBIE_HEAD);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.displayName(miniMessage.deserialize(configManager.getTextColorHighlight() + "<bold>" + entry.getKey() + "</bold><reset>"));
                List<Component> lore = new ArrayList<>();
                lore.add(miniMessage.deserialize(configManager.getTextColorLabel() + "Anzahl: " + configManager.getTextColorHighlight() + entry.getValue() + "<reset>"));
                meta.lore(lore);
                item.setItemMeta(meta);
            }
            inventory.setItem(slot++, item);
        }
        ItemStack backItem = new ItemStack(Material.ARROW);
        ItemMeta backMeta = backItem.getItemMeta();
        if (backMeta != null) {
            backMeta.displayName(miniMessage.deserialize(configManager.getTextColorLabel() + "<bold>Zurück</bold><reset>"));
            backItem.setItemMeta(backMeta);
        }
        inventory.setItem(49, backItem);
        player.openInventory(inventory);
    }
}
