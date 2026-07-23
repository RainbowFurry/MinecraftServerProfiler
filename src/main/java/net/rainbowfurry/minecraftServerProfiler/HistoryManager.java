package net.rainbowfurry.minecraftServerProfiler;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.util.*;

public class HistoryManager {
    private final MinecraftServerProfiler plugin;
    private final ConfigManager configManager;
    private final Gson gson = new Gson();
    private Connection connection;
    private BukkitRunnable saveTask;

    public HistoryManager(MinecraftServerProfiler plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        initializeDatabase();
        startPeriodicSaving();
    }

    private void initializeDatabase() {
        try {
            File databaseFile = new File(plugin.getDataFolder(), configManager.getHistoryDatabaseFile());
            if (!databaseFile.getParentFile().exists()) {
                databaseFile.getParentFile().mkdirs();
            }
            String url = "jdbc:sqlite:" + databaseFile.getAbsolutePath();
            connection = DriverManager.getConnection(url);
            createTable();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
        }
    }

    private void createTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS snapshots (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                timestamp INTEGER NOT NULL,
                tps REAL NOT NULL,
                mspt REAL NOT NULL,
                cpu_usage REAL NOT NULL,
                memory_used INTEGER NOT NULL,
                memory_max INTEGER NOT NULL,
                player_count INTEGER NOT NULL,
                total_tile_entities INTEGER NOT NULL,
                total_chunks INTEGER NOT NULL,
                world_chunk_data TEXT NOT NULL,
                total_redstone_components INTEGER NOT NULL,
                average_ping INTEGER NOT NULL,
                categories_data TEXT NOT NULL DEFAULT '{}',
                top_entity_data TEXT NOT NULL DEFAULT '{}',
                top_chunk_data TEXT NOT NULL DEFAULT '{}',
                top_plugin_data TEXT NOT NULL DEFAULT '{}',
                world_data TEXT NOT NULL DEFAULT '{}',
                entity_type_counts_data TEXT NOT NULL DEFAULT '{}',
                tile_entity_type_counts_data TEXT NOT NULL DEFAULT '{}'
            )
            """;
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            // Add columns if they don't exist (for existing databases)
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN total_redstone_components INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN average_ping INTEGER NOT NULL DEFAULT 0");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN categories_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN top_entity_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN top_chunk_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN top_plugin_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN world_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN entity_type_counts_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
            try {
                stmt.execute("ALTER TABLE snapshots ADD COLUMN tile_entity_type_counts_data TEXT NOT NULL DEFAULT '{}'");
            } catch (SQLException ignored) {}
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create snapshots table: " + e.getMessage());
        }
    }

    private void startPeriodicSaving() {
        if (!configManager.isHistoryEnabled()) {
            return;
        }
        long intervalTicks = configManager.getHistoryIntervalSeconds() * 20L;
        saveTask = new BukkitRunnable() {
            @Override
            public void run() {
                ProfilerManager profiler = plugin.getProfilerManager();
                saveSnapshotInternal(profiler);
            }
        };
        saveTask.runTaskTimer(plugin, intervalTicks, intervalTicks);
    }

    public void saveSnapshot() {
        ProfilerManager profiler = plugin.getProfilerManager();
        new BukkitRunnable() {
            @Override
            public void run() {
                saveSnapshotInternal(profiler);
            }
        }.runTaskAsynchronously(plugin);
    }

    private Map<String, Object> collectWorldData(ProfilerManager profiler) {
        Map<String, Object> worldData = new LinkedHashMap<>();
        for (World world : Bukkit.getWorlds()) {
            Map<String, Object> worldInfo = new LinkedHashMap<>();
            worldInfo.put("name", world.getName());
            worldInfo.put("chunkCount", world.getLoadedChunks().length);
            worldInfo.put("entityCount", world.getEntities().size());
            int tileEntityCount = 0;
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                tileEntityCount += chunk.getTileEntities().length;
            }
            worldInfo.put("tileEntityCount", tileEntityCount);
            worldData.put(world.getName(), worldInfo);
        }
        return worldData;
    }

    private Map<String, Long> collectEntityTypeCounts() {
        Map<String, Long> entityTypeCounts = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                String name = entity.getType().name();
                entityTypeCounts.put(name, entityTypeCounts.getOrDefault(name, 0L) + 1);
            }
        }
        return entityTypeCounts;
    }

    private Map<String, Long> collectTileEntityTypeCounts() {
        Map<String, Long> tileEntityTypeCounts = new HashMap<>();
        for (World world : Bukkit.getWorlds()) {
            for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    String name = tileEntity.getType().name();
                    tileEntityTypeCounts.put(name, tileEntityTypeCounts.getOrDefault(name, 0L) + 1);
                }
            }
        }
        return tileEntityTypeCounts;
    }

    private Map<String, Object> collectTopEntityData(ProfilerManager profiler) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map.Entry<String, Long> topEntity = profiler.getTopEntity();
        if (topEntity != null) {
            data.put("type", topEntity.getKey());
            data.put("count", topEntity.getValue());
        }
        return data;
    }

    private Map<String, Object> collectTopChunkData(ProfilerManager profiler) {
        Map<String, Object> data = new LinkedHashMap<>();
        ProfilerManager.DetailedChunkInfo topChunk = profiler.getDetailedTopChunk();
        if (topChunk != null) {
            data.put("worldName", topChunk.world.getName());
            data.put("x", topChunk.pos.x);
            data.put("z", topChunk.pos.z);
            data.put("totalEntities", topChunk.totalEntities);
            data.put("entityTypeCounts", topChunk.entityTypeCounts);
            data.put("tileEntityCount", topChunk.tileEntityCount);
            data.put("tileEntityTypeCounts", topChunk.tileEntityTypeCounts);
        }
        return data;
    }

    private Map<String, Object> collectTopPluginData(ProfilerManager profiler) {
        Map<String, Object> data = new LinkedHashMap<>();
        Map.Entry<String, Double> topPlugin = profiler.getTopPlugin();
        if (topPlugin != null) {
            data.put("name", topPlugin.getKey());
            data.put("timeMs", topPlugin.getValue());
        }
        return data;
    }

    private void saveSnapshotInternal(ProfilerManager profiler) {
        Map<String, Integer> worldChunkCounts = profiler.getWorldChunkCounts();
        int totalChunks = worldChunkCounts.values().stream().mapToInt(Integer::intValue).sum();
        String worldChunkData = gson.toJson(worldChunkCounts);
        double tps = profiler.getTPS();
        double mspt = profiler.getMSPT();
        double cpuUsage = profiler.getCpuUsage();
        long memoryUsed = profiler.getHeapMemoryUsage().getUsed();
        long memoryMax = profiler.getHeapMemoryUsage().getMax();
        int playerCount = profiler.getTotalPlayerCount();
        int totalTileEntities = profiler.getTotalTileEntities();
        int totalRedstoneComponents = profiler.getTotalRedstoneComponents();
        int averagePing = profiler.getAveragePing();
        String categoriesData = gson.toJson(profiler.calculateCategories());
        String topEntityData = gson.toJson(collectTopEntityData(profiler));
        String topChunkData = gson.toJson(collectTopChunkData(profiler));
        String topPluginData = gson.toJson(collectTopPluginData(profiler));
        String worldData = gson.toJson(collectWorldData(profiler));
        String entityTypeCountsData = gson.toJson(collectEntityTypeCounts());
        String tileEntityTypeCountsData = gson.toJson(collectTileEntityTypeCounts());

        String sql = """
            INSERT INTO snapshots (timestamp, tps, mspt, cpu_usage, memory_used, memory_max, player_count, total_tile_entities, total_chunks, world_chunk_data, total_redstone_components, average_ping, categories_data, top_entity_data, top_chunk_data, top_plugin_data, world_data, entity_type_counts_data, tile_entity_type_counts_data)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, System.currentTimeMillis());
            pstmt.setDouble(2, tps);
            pstmt.setDouble(3, mspt);
            pstmt.setDouble(4, cpuUsage);
            pstmt.setLong(5, memoryUsed);
            pstmt.setLong(6, memoryMax);
            pstmt.setInt(7, playerCount);
            pstmt.setInt(8, totalTileEntities);
            pstmt.setInt(9, totalChunks);
            pstmt.setString(10, worldChunkData);
            pstmt.setInt(11, totalRedstoneComponents);
            pstmt.setInt(12, averagePing);
            pstmt.setString(13, categoriesData);
            pstmt.setString(14, topEntityData);
            pstmt.setString(15, topChunkData);
            pstmt.setString(16, topPluginData);
            pstmt.setString(17, worldData);
            pstmt.setString(18, entityTypeCountsData);
            pstmt.setString(19, tileEntityTypeCountsData);
            pstmt.executeUpdate();
            deleteOldSnapshots();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save snapshot: " + e.getMessage());
        }
    }

    public List<Snapshot> getRecentSnapshots(int limit) {
        List<Snapshot> snapshots = new ArrayList<>();
        String sql = "SELECT * FROM snapshots ORDER BY timestamp DESC LIMIT ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                snapshots.add(new Snapshot(
                    rs.getLong("id"),
                    rs.getLong("timestamp"),
                    rs.getDouble("tps"),
                    rs.getDouble("mspt"),
                    rs.getDouble("cpu_usage"),
                    rs.getLong("memory_used"),
                    rs.getLong("memory_max"),
                    rs.getInt("player_count"),
                    rs.getInt("total_tile_entities"),
                    rs.getInt("total_chunks"),
                    rs.getString("world_chunk_data"),
                    rs.getInt("total_redstone_components"),
                    rs.getInt("average_ping"),
                    rs.getString("categories_data"),
                    rs.getString("top_entity_data"),
                    rs.getString("top_chunk_data"),
                    rs.getString("top_plugin_data"),
                    rs.getString("world_data"),
                    rs.getString("entity_type_counts_data"),
                    rs.getString("tile_entity_type_counts_data")
                ));
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get recent snapshots: " + e.getMessage());
        }
        return snapshots;
    }

    public Snapshot getSnapshotById(long id) {
        String sql = "SELECT * FROM snapshots WHERE id = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Snapshot(
                    rs.getLong("id"),
                    rs.getLong("timestamp"),
                    rs.getDouble("tps"),
                    rs.getDouble("mspt"),
                    rs.getDouble("cpu_usage"),
                    rs.getLong("memory_used"),
                    rs.getLong("memory_max"),
                    rs.getInt("player_count"),
                    rs.getInt("total_tile_entities"),
                    rs.getInt("total_chunks"),
                    rs.getString("world_chunk_data"),
                    rs.getInt("total_redstone_components"),
                    rs.getInt("average_ping"),
                    rs.getString("categories_data"),
                    rs.getString("top_entity_data"),
                    rs.getString("top_chunk_data"),
                    rs.getString("top_plugin_data"),
                    rs.getString("world_data"),
                    rs.getString("entity_type_counts_data"),
                    rs.getString("tile_entity_type_counts_data")
                );
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get snapshot by ID: " + e.getMessage());
        }
        return null;
    }

    public Snapshot getLatestSnapshot() {
        List<Snapshot> snapshots = getRecentSnapshots(1);
        return snapshots.isEmpty() ? null : snapshots.get(0);
    }

    private void deleteOldSnapshots() {
        int maxSnapshots = configManager.getMaxSnapshots();
        String sql = """
            DELETE FROM snapshots
            WHERE id NOT IN (
                SELECT id FROM snapshots ORDER BY timestamp DESC LIMIT ?
            )
            """;
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, maxSnapshots);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete old snapshots: " + e.getMessage());
        }
    }

    public void close() {
        if (saveTask != null) {
            saveTask.cancel();
        }
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database connection: " + e.getMessage());
        }
    }
}
