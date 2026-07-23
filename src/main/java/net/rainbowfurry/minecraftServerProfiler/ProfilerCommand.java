package net.rainbowfurry.minecraftServerProfiler;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.lang.management.MemoryUsage;
import java.util.Arrays;
import java.util.Map;
import java.util.List;

public class ProfilerCommand implements CommandExecutor, TabCompleter {

    private final ProfilerManager manager;
    private final ConfigManager configManager;
    private final GuiManager guiManager;
    private final HistoryManager historyManager;
    private final ExportManager exportManager;
    private final MinecraftServerProfiler plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public ProfilerCommand(ProfilerManager manager, ConfigManager configManager, GuiManager guiManager, HistoryManager historyManager, ExportManager exportManager, MinecraftServerProfiler plugin) {
        this.manager = manager;
        this.configManager = configManager;
        this.guiManager = guiManager;
        this.historyManager = historyManager;
        this.exportManager = exportManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("gui")) {
                if (sender instanceof org.bukkit.entity.Player player) {
                    guiManager.openMainGui(player);
                } else {
                    send(sender, "<red>Nur Spieler können das GUI öffnen!<reset>");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("tptop")) {
                if (sender instanceof Player player) {
                    ProfilerManager.DetailedChunkInfo topChunk = manager.getDetailedTopChunk();
                    if (topChunk != null) {
                        Location loc = new Location(topChunk.world, (topChunk.pos.x << 4) + 8, 0, (topChunk.pos.z << 4) + 8);
                        loc.setY(topChunk.world.getHighestBlockYAt(loc) + 1);
                        player.teleport(loc);
                        send(sender, configManager.getTextColorHighlight() + "Teleportiert zum Top Chunk in Welt " + topChunk.world.getName() + " (" + topChunk.pos.x + ", " + topChunk.pos.z + ")!");
                    } else {
                        send(sender, configManager.getTextColorNone() + "Kein Top Chunk gefunden!");
                    }
                } else {
                    send(sender, "<red>Nur Spieler können teleportieren!<reset>");
                }
                return true;
            }
            if (args[0].equalsIgnoreCase("save")) {
                historyManager.saveSnapshot();
                send(sender, configManager.getTextColorHighlight() + "Snapshot gespeichert!");
                return true;
            }
            if (args[0].equalsIgnoreCase("help")) {
                sendHelp(sender);
                return true;
            }
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("export")) {
                String format = args[1].toLowerCase();
                if (format.equals("json") || format.equals("csv") || format.equals("md")) {
                    exportCurrentSnapshot(sender, format);
                } else {
                    send(sender, "<red>Ungültiges Format! Nutze json, csv oder md!<reset>");
                }
                return true;
            }
        }
        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("export")) {
                String format = args[1].toLowerCase();
                try {
                    long id = Long.parseLong(args[2]);
                    exportSnapshotById(sender, format, id);
                } catch (NumberFormatException e) {
                    send(sender, "<red>Ungültige ID!<reset>");
                }
                return true;
            }
        }
        sendProfilerInfo(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        send(sender, configManager.getGradientHeader() + "========== <bold>SERVER PROFILER - HILFE</bold> ==========</gradient>");
        send(sender, "");
        send(sender, configManager.getTextColorLabel() + "/profiler" + configManager.getTextColorNone() + " - Zeigt Performance-Informationen an");
        send(sender, configManager.getTextColorLabel() + "/profiler gui" + configManager.getTextColorNone() + " - Öffnet das GUI");
        send(sender, configManager.getTextColorLabel() + "/profiler tptop" + configManager.getTextColorNone() + " - Teleportiert zum höchsten Chunk");
        send(sender, configManager.getTextColorLabel() + "/profiler save" + configManager.getTextColorNone() + " - Speichert aktuellen Snapshot");
        send(sender, configManager.getTextColorLabel() + "/profiler export <json/csv/md> [id]" + configManager.getTextColorNone() + " - Exportiert Snapshot");
        send(sender, configManager.getTextColorLabel() + "/profiler help" + configManager.getTextColorNone() + " - Zeigt diese Hilfe an");
        send(sender, "");
        send(sender, configManager.getGradientHeader() + "============================================</gradient>");
    }

    private void exportCurrentSnapshot(CommandSender sender, String format) {
        if (!configManager.isExportEnabled()) {
            send(sender, "<red>Exporte sind in der Config deaktiviert!<reset>");
            return;
        }
        send(sender, configManager.getTextColorHighlight() + "Snapshot wird erstellt und exportiert...");
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                historyManager.saveSnapshot();
                Snapshot snapshot = historyManager.getLatestSnapshot();
                if (snapshot != null) {
                    exportSnapshot(sender, snapshot, format);
                } else {
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            send(sender, "<red>Kein Snapshot gefunden!<reset>");
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void exportSnapshotById(CommandSender sender, String format, long id) {
        if (!configManager.isExportEnabled()) {
            send(sender, "<red>Exporte sind in der Config deaktiviert!<reset>");
            return;
        }
        send(sender, configManager.getTextColorHighlight() + "Exportiere Snapshot mit ID " + id + "...");
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                Snapshot snapshot = historyManager.getSnapshotById(id);
                if (snapshot != null) {
                    exportSnapshot(sender, snapshot, format);
                } else {
                    new org.bukkit.scheduler.BukkitRunnable() {
                        @Override
                        public void run() {
                            send(sender, "<red>Snapshot mit ID " + id + " nicht gefunden!<reset>");
                        }
                    }.runTask(plugin);
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void exportSnapshot(CommandSender sender, Snapshot snapshot, String format) {
        try {
            java.io.File file;
            switch (format) {
                case "json":
                    file = exportManager.exportToJson(snapshot);
                    break;
                case "csv":
                    file = exportManager.exportToCsv(snapshot);
                    break;
                case "md":
                    file = exportManager.exportToMarkdown(snapshot);
                    break;
                default:
                    file = null;
            }
            final java.io.File finalFile = file;
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    send(sender, configManager.getTextColorHighlight() + "Export erfolgreich gespeichert in: " + finalFile.getAbsolutePath());
                }
            }.runTask(plugin);
        } catch (java.io.IOException e) {
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    send(sender, "<red>Fehler beim Exportieren: " + e.getMessage() + "<reset>");
                }
            }.runTask(plugin);
        }
    }

    private void sendProfilerInfo(CommandSender sender) {
        double tps = manager.getTPS();
        double mspt = manager.getMSPT();
        MemoryUsage heapUsage = manager.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = manager.getNonHeapMemoryUsage();
        double cpuUsage = manager.getCpuUsage();
        int playerCount = manager.getTotalPlayerCount();
        int tileEntityCount = manager.getTotalTileEntities();
        int redstoneCount = manager.getTotalRedstoneComponents();
        int averagePing = manager.getAveragePing();
        Map<String, Integer> worldChunkCounts = manager.getWorldChunkCounts();
        Map<String, Double> categories = manager.calculateCategories();
        Map.Entry<String, Long> topEntity = manager.getTopEntity();
        ProfilerManager.DetailedChunkInfo topChunk = manager.getDetailedTopChunk();
        Map.Entry<String, Double> topPlugin = manager.getTopPlugin();

        String labelColor = configManager.getTextColorLabel();
        String labelClose = labelColor.replace("<", "</");
        String highlightColor = configManager.getTextColorHighlight();
        String highlightClose = highlightColor.replace("<", "</");
        String noneColor = configManager.getTextColorNone();
        String noneClose = noneColor.replace("<", "</");

        send(sender, configManager.getGradientHeader() + "============== <bold>SERVER PROFILER</bold> ==============</gradient>");
        send(sender, "");
        send(sender, labelColor + "TPS: " + labelClose + getColoredTPS(tps));
        send(sender, labelColor + "MSPT: " + labelClose + getColoredMSPT(mspt));
        send(sender, "");
        send(sender, configManager.getGradientSystem() + "-- System --</gradient>");
        send(sender, labelColor + "CPU Usage: " + labelClose + getColoredPercentage(cpuUsage >= 0 ? cpuUsage : 0));
        send(sender, labelColor + "Heap Memory: " + labelClose + getColoredMemory(heapUsage.getUsed(), heapUsage.getMax()));
        send(sender, labelColor + "Non-Heap Memory: " + labelClose + getColoredMemory(nonHeapUsage.getUsed(), nonHeapUsage.getMax()));
        send(sender, "");
        send(sender, configManager.getGradientGame() + "-- Game --</gradient>");
        send(sender, labelColor + "Online Players: " + labelClose + getColoredNumber(playerCount, configManager.getPlayersThresholdGood(), configManager.getPlayersThresholdModerate(), configManager.getPlayersThresholdBad()));
        send(sender, labelColor + "Tile Entities: " + labelClose + getColoredNumber(tileEntityCount, configManager.getTileEntitiesThresholdGood(), configManager.getTileEntitiesThresholdModerate(), configManager.getTileEntitiesThresholdBad()));
        send(sender, "");
        send(sender, configManager.getGradientWorlds() + "-- Worlds --</gradient>");
        for (Map.Entry<String, Integer> entry : worldChunkCounts.entrySet()) {
            send(sender, labelColor + entry.getKey() + ": " + labelClose + getColoredNumber(entry.getValue(), configManager.getChunksThresholdGood(), configManager.getChunksThresholdModerate(), configManager.getChunksThresholdBad()) + " chunks");
        }
        send(sender, "");
        send(sender, configManager.getGradientBreakdown() + "-- Breakdown --</gradient>");
        for (Map.Entry<String, Double> entry : categories.entrySet()) {
            send(sender, labelColor + String.format("%-13s", entry.getKey()) + ": " + labelClose + getColoredPercentage(entry.getValue()));
        }
        send(sender, "");
        send(sender, configManager.getGradientTop() + "Top Entity:</gradient>");
        if (topEntity != null) {
            send(sender, highlightColor + topEntity.getKey() + " (" + configManager.getStatusColorModerate() + topEntity.getValue() + configManager.getStatusColorModerate().replace("<", "</") + highlightClose + ")");
        } else {
            send(sender, noneColor + "None" + noneClose);
        }
        send(sender, "");
        send(sender, configManager.getGradientTop() + "Top Chunk:</gradient>");
        if (topChunk != null) {
            send(sender, highlightColor + "Welt: " + configManager.getStatusColorModerate() + topChunk.world.getName() + configManager.getStatusColorModerate().replace("<", "</") + highlightClose);
            send(sender, highlightColor + "Position: " + configManager.getStatusColorModerate() + topChunk.pos.x + ", " + topChunk.pos.z + configManager.getStatusColorModerate().replace("<", "</") + highlightClose);
            send(sender, highlightColor + "Gesamt Entities: " + configManager.getStatusColorModerate() + topChunk.totalEntities + configManager.getStatusColorModerate().replace("<", "</") + highlightClose);
            send(sender, highlightColor + "Tile Entities: " + configManager.getStatusColorModerate() + topChunk.tileEntityCount + configManager.getStatusColorModerate().replace("<", "</") + highlightClose);
            send(sender, highlightColor + "Entity-Typen:");
            List<Map.Entry<String, Integer>> sortedEntities = topChunk.entityTypeCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(5)
                    .toList();
            for (Map.Entry<String, Integer> entry : sortedEntities) {
                send(sender, "  " + configManager.getTextColorLabel() + entry.getKey() + ": " + configManager.getStatusColorModerate() + entry.getValue() + configManager.getStatusColorModerate().replace("<", "</"));
            }
            if (!topChunk.tileEntityTypeCounts.isEmpty()) {
                send(sender, highlightColor + "Tile-Entity-Typen:");
                List<Map.Entry<String, Integer>> sortedTileEntities = topChunk.tileEntityTypeCounts.entrySet().stream()
                        .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                        .limit(5)
                        .toList();
                for (Map.Entry<String, Integer> entry : sortedTileEntities) {
                    send(sender, "  " + configManager.getTextColorLabel() + entry.getKey() + ": " + configManager.getStatusColorModerate() + entry.getValue() + configManager.getStatusColorModerate().replace("<", "</"));
                }
            }
        } else {
            send(sender, noneColor + "None" + noneClose);
        }
        send(sender, "");
        send(sender, configManager.getGradientTop() + "Top Plugin:</gradient>");
        if (topPlugin != null) {
            send(sender, highlightColor + topPlugin.getKey() + " (" + configManager.getStatusColorModerate() + String.format("%.2f", topPlugin.getValue()) + "ms" + configManager.getStatusColorModerate().replace("<", "</") + highlightClose + ")");
        } else {
            send(sender, noneColor + "None" + noneClose);
        }
        send(sender, "");
        send(sender, configManager.getGradientHeader() + "=============================================</gradient>");
    }

    private String getColoredTPS(double tps) {
        if (tps >= configManager.getTpsThresholdGood()) return configManager.getStatusColorGood() + String.format("%.2f", tps) + configManager.getStatusColorGood().replace("<", "</");
        if (tps >= configManager.getTpsThresholdModerate()) return configManager.getStatusColorModerate() + String.format("%.2f", tps) + configManager.getStatusColorModerate().replace("<", "</");
        if (tps >= configManager.getTpsThresholdBad()) return configManager.getStatusColorBad() + String.format("%.2f", tps) + configManager.getStatusColorBad().replace("<", "</");
        return configManager.getStatusColorCritical() + String.format("%.2f", tps) + configManager.getStatusColorCritical().replace("<", "</");
    }

    private String getColoredMSPT(double mspt) {
        if (mspt <= configManager.getMsptThresholdGood()) return configManager.getStatusColorGood() + String.format("%.2f", mspt) + configManager.getStatusColorGood().replace("<", "</");
        if (mspt <= configManager.getMsptThresholdModerate()) return configManager.getStatusColorModerate() + String.format("%.2f", mspt) + configManager.getStatusColorModerate().replace("<", "</");
        if (mspt <= configManager.getMsptThresholdBad()) return configManager.getStatusColorBad() + String.format("%.2f", mspt) + configManager.getStatusColorBad().replace("<", "</");
        return configManager.getStatusColorCritical() + String.format("%.2f", mspt) + configManager.getStatusColorCritical().replace("<", "</");
    }

    private String getColoredPercentage(double percent) {
        if (percent <= configManager.getPercentageThresholdGood()) return configManager.getStatusColorGood() + String.format("%.0f%%", percent) + configManager.getStatusColorGood().replace("<", "</");
        if (percent <= configManager.getPercentageThresholdModerate()) return configManager.getStatusColorModerate() + String.format("%.0f%%", percent) + configManager.getStatusColorModerate().replace("<", "</");
        if (percent <= configManager.getPercentageThresholdBad()) return configManager.getStatusColorBad() + String.format("%.0f%%", percent) + configManager.getStatusColorBad().replace("<", "</");
        return configManager.getStatusColorCritical() + String.format("%.0f%%", percent) + configManager.getStatusColorCritical().replace("<", "</");
    }

    private String getColoredNumber(long number, long good, long moderate, long bad) {
        if (number <= good) return configManager.getStatusColorGood() + number + configManager.getStatusColorGood().replace("<", "</");
        if (number <= moderate) return configManager.getStatusColorModerate() + number + configManager.getStatusColorModerate().replace("<", "</");
        if (number <= bad) return configManager.getStatusColorBad() + number + configManager.getStatusColorBad().replace("<", "</");
        return configManager.getStatusColorCritical() + number + configManager.getStatusColorCritical().replace("<", "</");
    }

    private String getColoredMemory(long used, long max) {
        double percent = max > 0 ? ((double) used / max) * 100 : 0;
        String usedStr = ProfilerManager.formatBytes(used);
        String maxStr = ProfilerManager.formatBytes(max);
        if (percent <= configManager.getMemoryThresholdGood()) return configManager.getStatusColorGood() + usedStr + " / " + maxStr + configManager.getStatusColorGood().replace("<", "</");
        if (percent <= configManager.getMemoryThresholdModerate()) return configManager.getStatusColorModerate() + usedStr + " / " + maxStr + configManager.getStatusColorModerate().replace("<", "</");
        if (percent <= configManager.getMemoryThresholdBad()) return configManager.getStatusColorBad() + usedStr + " / " + maxStr + configManager.getStatusColorBad().replace("<", "</");
        return configManager.getStatusColorCritical() + usedStr + " / " + maxStr + configManager.getStatusColorCritical().replace("<", "</");
    }

    private void send(CommandSender sender, String miniMessageString) {
        Component component = miniMessage.deserialize(miniMessageString);
        sender.sendMessage(component);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return Arrays.asList("help", "gui", "tptop", "save", "export").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("export")) {
            return Arrays.asList("json", "csv", "md").stream()
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .toList();
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("export")) {
            List<Snapshot> snapshots = historyManager.getRecentSnapshots(100);
            return snapshots.stream()
                    .map(s -> String.valueOf(s.id()))
                    .filter(s -> s.startsWith(args[2]))
                    .toList();
        }
        return List.of();
    }
}
