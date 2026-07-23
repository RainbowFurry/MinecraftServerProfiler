package net.rainbowfurry.minecraftServerProfiler;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.block.BlockState;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.util.*;

public class ProfilerManager {
    private final MinecraftServerProfiler plugin;
    private final PluginTimingsManager pluginTimingsManager;

    public ProfilerManager(MinecraftServerProfiler plugin, PluginTimingsManager pluginTimingsManager) {
        this.plugin = plugin;
        this.pluginTimingsManager = pluginTimingsManager;
    }
    
    public PluginTimingsManager getPluginTimingsManager() {
        return pluginTimingsManager;
    }

    public double getTPS() {
        return Bukkit.getServer().getTPS()[0];
    }

    public double getMSPT() {
        return Bukkit.getServer().getAverageTickTime();
    }

    public MemoryUsage getHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getHeapMemoryUsage();
    }

    public MemoryUsage getNonHeapMemoryUsage() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        return memoryMXBean.getNonHeapMemoryUsage();
    }

    public double getCpuUsage() {
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad() * 100;
        }
        return -1;
    }

    public int getTotalPlayerCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    public Map<String, Integer> getWorldChunkCounts() {
        Map<String, Integer> worldChunkCounts = new LinkedHashMap<>();
        for (World world : Bukkit.getWorlds()) {
            worldChunkCounts.put(world.getName(), world.getLoadedChunks().length);
        }
        return worldChunkCounts;
    }

    public int getTotalTileEntities() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                count += chunk.getTileEntities().length;
            }
        }
        return count;
    }

    public Map<String, Double> calculateCategories() {
        Map<String, Double> categories = new LinkedHashMap<>();
        int totalEntities = Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getEntities().size())
                .sum();
        int totalChunks = Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getLoadedChunks().length)
                .sum();
        int livingEntities = Bukkit.getWorlds().stream()
                .mapToInt(w -> w.getLivingEntities().size())
                .sum();
        int hopperCount = (int) Bukkit.getWorlds().stream()
                .flatMap(w -> w.getEntities().stream())
                .filter(e -> e.getType().name().equals("HOPPER_MINECART") || e.getType().name().equals("HOPPER"))
                .count();
        int redstoneCount = getTotalRedstoneComponents();

        double entitiesPercent = Math.min(45.0, 10.0 + (totalEntities / 10.0));
        double mobAiPercent = Math.min(25.0, 5.0 + (livingEntities / 8.0));
        double pathfindingPercent = Math.min(15.0, 3.0 + (livingEntities / 12.0));
        double chunksPercent = Math.min(15.0, 2.0 + (totalChunks / 20.0));
        double hopperPercent = Math.min(10.0, 1.0 + (hopperCount / 5.0));
        double redstonePercent = Math.min(8.0, 2.0 + (redstoneCount / 500.0));
        double schedulerPercent = 3.0;
        double pluginsPercent = 2.0;
        double networkPercent = Math.min(5.0, 0.5 + (getTotalPlayerCount() / 5.0));
        double gcPercent = 3.0;

        double total = entitiesPercent + mobAiPercent + pathfindingPercent + chunksPercent + hopperPercent + redstonePercent + schedulerPercent + pluginsPercent + networkPercent + gcPercent;

        categories.put("Entities", (entitiesPercent / total) * 100);
        categories.put("Mob AI", (mobAiPercent / total) * 100);
        categories.put("Pathfinding", (pathfindingPercent / total) * 100);
        categories.put("Chunks", (chunksPercent / total) * 100);
        categories.put("Hopper", (hopperPercent / total) * 100);
        categories.put("Redstone", (redstonePercent / total) * 100);
        categories.put("Scheduler", (schedulerPercent / total) * 100);
        categories.put("Plugins", (pluginsPercent / total) * 100);
        categories.put("Network", (networkPercent / total) * 100);
        categories.put("GC", (gcPercent / total) * 100);

        return categories;
    }

    public Map.Entry<String, Long> getTopEntity() {
        Map<String, Long> entityCounts = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String name = entity.getType().name();
                entityCounts.put(name, entityCounts.getOrDefault(name, 0L) + 1);
            }
        }
        return entityCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);
    }

    public static class ChunkPos {
        int x, z;

        public ChunkPos(int x, int z) {
            this.x = x;
            this.z = z;
        }
    }

    public static class DetailedChunkInfo {
        World world;
        ChunkPos pos;
        int totalEntities;
        Map<String, Integer> entityTypeCounts;
        int tileEntityCount;
        Map<String, Integer> tileEntityTypeCounts;

        public DetailedChunkInfo(World world, ChunkPos pos, int totalEntities, Map<String, Integer> entityTypeCounts, int tileEntityCount, Map<String, Integer> tileEntityTypeCounts) {
            this.world = world;
            this.pos = pos;
            this.totalEntities = totalEntities;
            this.entityTypeCounts = entityTypeCounts;
            this.tileEntityCount = tileEntityCount;
            this.tileEntityTypeCounts = tileEntityTypeCounts;
        }
    }

    public DetailedChunkInfo getDetailedTopChunk() {
        Map<World, Map<ChunkPos, Integer>> chunkEntityCounts = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            Map<ChunkPos, Integer> worldCounts = new HashMap<>();
            for (Entity entity : world.getEntities()) {
                int cx = entity.getLocation().getBlockX() >> 4;
                int cz = entity.getLocation().getBlockZ() >> 4;
                ChunkPos pos = new ChunkPos(cx, cz);
                worldCounts.put(pos, worldCounts.getOrDefault(pos, 0) + 1);
            }
            chunkEntityCounts.put(world, worldCounts);
        }

        World topWorld = null;
        Map.Entry<ChunkPos, Integer> topChunkEntry = null;

        for (Map.Entry<World, Map<ChunkPos, Integer>> worldEntry : chunkEntityCounts.entrySet()) {
            Map.Entry<ChunkPos, Integer> maxEntry = worldEntry.getValue().entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .orElse(null);
            if (maxEntry != null && (topChunkEntry == null || maxEntry.getValue() > topChunkEntry.getValue())) {
                topWorld = worldEntry.getKey();
                topChunkEntry = maxEntry;
            }
        }

        if (topWorld != null && topChunkEntry != null) {
            Chunk chunk = topWorld.getChunkAt(topChunkEntry.getKey().x, topChunkEntry.getKey().z);
            Map<String, Integer> entityTypeCounts = new HashMap<>();
            for (Entity entity : chunk.getEntities()) {
                String name = entity.getType().name();
                entityTypeCounts.put(name, entityTypeCounts.getOrDefault(name, 0) + 1);
            }
            Map<String, Integer> tileEntityTypeCounts = new HashMap<>();
            for (BlockState tileEntity : chunk.getTileEntities()) {
                String name = tileEntity.getType().name();
                tileEntityTypeCounts.put(name, tileEntityTypeCounts.getOrDefault(name, 0) + 1);
            }
            return new DetailedChunkInfo(topWorld, topChunkEntry.getKey(), topChunkEntry.getValue(), entityTypeCounts, chunk.getTileEntities().length, tileEntityTypeCounts);
        }
        return null;
    }

    @Deprecated
    public Map.Entry<World, ChunkPos> getTopChunk() {
        DetailedChunkInfo info = getDetailedTopChunk();
        if (info != null) {
            return new AbstractMap.SimpleEntry<>(info.world, info.pos);
        }
        return null;
    }

    public Map.Entry<String, Double> getTopPlugin() {
        List<PluginTiming> topPlugins = pluginTimingsManager.getTopPlugins(1);
        if (topPlugins.isEmpty()) {
            return null;
        }
        PluginTiming top = topPlugins.get(0);
        return new AbstractMap.SimpleEntry<>(top.pluginName(), (double) top.totalTimeMs());
    }

    public int getTotalRedstoneComponents() {
        int count = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Chunk chunk : world.getLoadedChunks()) {
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    String typeName = tileEntity.getType().name();
                    if (typeName.contains("REDSTONE") || typeName.contains("REPEATER") || 
                        typeName.contains("COMPARATOR") || typeName.contains("PISTON") ||
                        typeName.contains("OBSERVER") || typeName.contains("DROPPER") ||
                        typeName.contains("DISPENSER") || typeName.contains("HOPPER")) {
                        count++;
                    }
                }
            }
        }
        return count;
    }
    
    public int getAveragePing() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            return 0;
        }
        int totalPing = 0;
        for (org.bukkit.entity.Player player : Bukkit.getOnlinePlayers()) {
            totalPing += player.getPing();
        }
        return totalPing / Bukkit.getOnlinePlayers().size();
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
