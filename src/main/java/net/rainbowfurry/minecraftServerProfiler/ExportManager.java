package net.rainbowfurry.minecraftServerProfiler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

public class ExportManager {
    private final MinecraftServerProfiler plugin;
    private final ConfigManager configManager;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    public ExportManager(MinecraftServerProfiler plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        createExportDirectory();
    }

    private void createExportDirectory() {
        File exportDir = new File(plugin.getDataFolder(), configManager.getExportDirectory());
        if (!exportDir.exists()) {
            exportDir.mkdirs();
        }
    }

    private File getExportDirectory() {
        return new File(plugin.getDataFolder(), configManager.getExportDirectory());
    }

    public File exportToJson(Snapshot snapshot) throws IOException {
        File exportDir = getExportDirectory();
        String filename = "snapshot_" + snapshot.id() + "_" + dateFormat.format(new Date(snapshot.timestamp())) + ".json";
        File file = new File(exportDir, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(snapshot, writer);
        }
        
        return file;
    }

    public File exportToCsv(Snapshot snapshot) throws IOException {
        File exportDir = getExportDirectory();
        String filename = "snapshot_" + snapshot.id() + "_" + dateFormat.format(new Date(snapshot.timestamp())) + ".csv";
        File file = new File(exportDir, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("ID,Timestamp,Date,TPS,MSPT,CPU Usage,Memory Used,Memory Max,Player Count,Tile Entities,Chunks,Redstone Components,Average Ping\n");
            writer.write(String.format("%d,%d,%s,%.2f,%.2f,%.2f,%d,%d,%d,%d,%d,%d,%d\n",
                snapshot.id(),
                snapshot.timestamp(),
                dateFormat.format(new Date(snapshot.timestamp())),
                snapshot.tps(),
                snapshot.mspt(),
                snapshot.cpuUsage(),
                snapshot.memoryUsed(),
                snapshot.memoryMax(),
                snapshot.playerCount(),
                snapshot.totalTileEntities(),
                snapshot.totalChunks(),
                snapshot.totalRedstoneComponents(),
                snapshot.averagePing()
            ));
            
            writer.write("\nWorld,Chunk Count,Entity Count,Tile Entity Count\n");
            Type worldType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> worldData = gson.fromJson(snapshot.worldData(), worldType);
            if (worldData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : worldData.entrySet()) {
                    Map<String, Object> worldInfo = entry.getValue();
                    writer.write(String.format("%s,%d,%d,%d\n",
                        entry.getKey(),
                        worldInfo.get("chunkCount"),
                        worldInfo.get("entityCount"),
                        worldInfo.get("tileEntityCount")
                    ));
                }
            }
            
            writer.write("\nPerformance Categories\n");
            Type categoriesType = new TypeToken<Map<String, Double>>() {}.getType();
            Map<String, Double> categories = gson.fromJson(snapshot.categoriesData(), categoriesType);
            if (categories != null) {
                for (Map.Entry<String, Double> entry : categories.entrySet()) {
                    writer.write(String.format("%s,%.2f%%\n", entry.getKey(), entry.getValue()));
                }
            }
            
            writer.write("\nEntity Type Counts\n");
            Type entityTypeCountsType = new TypeToken<Map<String, Long>>() {}.getType();
            Map<String, Long> entityTypeCounts = gson.fromJson(snapshot.entityTypeCountsData(), entityTypeCountsType);
            if (entityTypeCounts != null) {
                for (Map.Entry<String, Long> entry : entityTypeCounts.entrySet()) {
                    writer.write(String.format("%s,%d\n", entry.getKey(), entry.getValue()));
                }
            }
            
            writer.write("\nTile Entity Type Counts\n");
            Type tileEntityTypeCountsType = new TypeToken<Map<String, Long>>() {}.getType();
            Map<String, Long> tileEntityTypeCounts = gson.fromJson(snapshot.tileEntityTypeCountsData(), tileEntityTypeCountsType);
            if (tileEntityTypeCounts != null) {
                for (Map.Entry<String, Long> entry : tileEntityTypeCounts.entrySet()) {
                    writer.write(String.format("%s,%d\n", entry.getKey(), entry.getValue()));
                }
            }
        }
        
        return file;
    }

    public File exportToMarkdown(Snapshot snapshot) throws IOException {
        File exportDir = getExportDirectory();
        String filename = "snapshot_" + snapshot.id() + "_" + dateFormat.format(new Date(snapshot.timestamp())) + ".md";
        File file = new File(exportDir, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# Server Snapshot Report\n\n");
            writer.write("**Snapshot ID:** " + snapshot.id() + "  \n");
            writer.write("**Date:** " + dateFormat.format(new Date(snapshot.timestamp())) + "  \n");
            writer.write("**Timestamp:** " + snapshot.timestamp() + "\n\n");
            
            writer.write("## Server Performance\n\n");
            writer.write("| Metric | Value |\n");
            writer.write("|--------|-------|\n");
            writer.write(String.format("| TPS | %.2f |\n", snapshot.tps()));
            writer.write(String.format("| MSPT | %.2f |\n", snapshot.mspt()));
            writer.write(String.format("| CPU Usage | %.2f%% |\n", snapshot.cpuUsage()));
            writer.write(String.format("| Memory Used | %s |\n", formatBytes(snapshot.memoryUsed())));
            writer.write(String.format("| Memory Max | %s |\n", formatBytes(snapshot.memoryMax())));
            writer.write(String.format("| Player Count | %d |\n", snapshot.playerCount()));
            writer.write(String.format("| Tile Entities | %d |\n", snapshot.totalTileEntities()));
            writer.write(String.format("| Total Chunks | %d |\n", snapshot.totalChunks()));
            writer.write(String.format("| Redstone Components | %d |\n", snapshot.totalRedstoneComponents()));
            writer.write(String.format("| Average Ping | %d ms |\n\n", snapshot.averagePing()));
            
            writer.write("## Performance Categories\n\n");
            Type categoriesType = new TypeToken<Map<String, Double>>() {}.getType();
            Map<String, Double> categories = gson.fromJson(snapshot.categoriesData(), categoriesType);
            if (categories != null) {
                writer.write("| Category | Percentage |\n");
                writer.write("|----------|------------|\n");
                for (Map.Entry<String, Double> entry : categories.entrySet()) {
                    writer.write(String.format("| %s | %.2f%% |\n", entry.getKey(), entry.getValue()));
                }
            }
            
            writer.write("\n## World Data\n\n");
            writer.write("| World | Chunks | Entities | Tile Entities |\n");
            writer.write("|-------|--------|----------|---------------|\n");
            Type worldType = new TypeToken<Map<String, Map<String, Object>>>() {}.getType();
            Map<String, Map<String, Object>> worldData = gson.fromJson(snapshot.worldData(), worldType);
            if (worldData != null) {
                for (Map.Entry<String, Map<String, Object>> entry : worldData.entrySet()) {
                    Map<String, Object> worldInfo = entry.getValue();
                    writer.write(String.format("| %s | %d | %d | %d |\n",
                        entry.getKey(),
                        worldInfo.get("chunkCount"),
                        worldInfo.get("entityCount"),
                        worldInfo.get("tileEntityCount")
                    ));
                }
            }
            
            writer.write("\n## Top Entity\n\n");
            Type topEntityType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> topEntityData = gson.fromJson(snapshot.topEntityData(), topEntityType);
            if (topEntityData != null && !topEntityData.isEmpty()) {
                writer.write(String.format("- **Type:** %s\n", topEntityData.get("type")));
                writer.write(String.format("- **Count:** %d\n\n", topEntityData.get("count")));
            } else {
                writer.write("- No data available\n\n");
            }
            
            writer.write("\n## Top Chunk\n\n");
            Type topChunkType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> topChunkData = gson.fromJson(snapshot.topChunkData(), topChunkType);
            if (topChunkData != null && !topChunkData.isEmpty()) {
                writer.write(String.format("- **World:** %s\n", topChunkData.get("worldName")));
                writer.write(String.format("- **Position:** %d, %d\n", topChunkData.get("x"), topChunkData.get("z")));
                writer.write(String.format("- **Total Entities:** %d\n", topChunkData.get("totalEntities")));
                writer.write(String.format("- **Tile Entities:** %d\n\n", topChunkData.get("tileEntityCount")));
                
                @SuppressWarnings("unchecked")
                Map<String, Integer> entityTypeCounts = (Map<String, Integer>) topChunkData.get("entityTypeCounts");
                if (entityTypeCounts != null && !entityTypeCounts.isEmpty()) {
                    writer.write("### Entity Types in Top Chunk\n\n");
                    writer.write("| Type | Count |\n");
                    writer.write("|------|-------|\n");
                    for (Map.Entry<String, Integer> entry : entityTypeCounts.entrySet()) {
                        writer.write(String.format("| %s | %d |\n", entry.getKey(), entry.getValue()));
                    }
                }
                
                @SuppressWarnings("unchecked")
                Map<String, Integer> tileEntityTypeCounts = (Map<String, Integer>) topChunkData.get("tileEntityTypeCounts");
                if (tileEntityTypeCounts != null && !tileEntityTypeCounts.isEmpty()) {
                    writer.write("\n### Tile Entity Types in Top Chunk\n\n");
                    writer.write("| Type | Count |\n");
                    writer.write("|------|-------|\n");
                    for (Map.Entry<String, Integer> entry : tileEntityTypeCounts.entrySet()) {
                        writer.write(String.format("| %s | %d |\n", entry.getKey(), entry.getValue()));
                    }
                }
            } else {
                writer.write("- No data available\n\n");
            }
            
            writer.write("\n## Top Plugin\n\n");
            Type topPluginType = new TypeToken<Map<String, Object>>() {}.getType();
            Map<String, Object> topPluginData = gson.fromJson(snapshot.topPluginData(), topPluginType);
            if (topPluginData != null && !topPluginData.isEmpty()) {
                writer.write(String.format("- **Name:** %s\n", topPluginData.get("name")));
                writer.write(String.format("- **Time:** %.2f ms\n\n", topPluginData.get("timeMs")));
            } else {
                writer.write("- No data available\n\n");
            }
            
            writer.write("\n## Entity Type Counts\n\n");
            Type entityTypeCountsType = new TypeToken<Map<String, Long>>() {}.getType();
            Map<String, Long> entityTypeCounts = gson.fromJson(snapshot.entityTypeCountsData(), entityTypeCountsType);
            if (entityTypeCounts != null) {
                writer.write("| Entity Type | Count |\n");
                writer.write("|-------------|-------|\n");
                for (Map.Entry<String, Long> entry : entityTypeCounts.entrySet()) {
                    writer.write(String.format("| %s | %d |\n", entry.getKey(), entry.getValue()));
                }
            }
            
            writer.write("\n## Tile Entity Type Counts\n\n");
            Type tileEntityTypeCountsType = new TypeToken<Map<String, Long>>() {}.getType();
            Map<String, Long> tileEntityTypeCounts = gson.fromJson(snapshot.tileEntityTypeCountsData(), tileEntityTypeCountsType);
            if (tileEntityTypeCounts != null) {
                writer.write("| Tile Entity Type | Count |\n");
                writer.write("|------------------|-------|\n");
                for (Map.Entry<String, Long> entry : tileEntityTypeCounts.entrySet()) {
                    writer.write(String.format("| %s | %d |\n", entry.getKey(), entry.getValue()));
                }
            }
        }
        
        return file;
    }

    public File compareSnapshots(Snapshot snapshot1, Snapshot snapshot2) throws IOException {
        File exportDir = getExportDirectory();
        String filename = String.format("compare_%d_vs_%d_%s.md", 
            snapshot1.id(), snapshot2.id(), dateFormat.format(new Date()));
        File file = new File(exportDir, filename);
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("# Snapshot Comparison Report\n\n");
            writer.write("## Snapshot 1\n");
            writer.write("- **ID:** " + snapshot1.id() + "\n");
            writer.write("- **Date:** " + dateFormat.format(new Date(snapshot1.timestamp())) + "\n\n");
            
            writer.write("## Snapshot 2\n");
            writer.write("- **ID:** " + snapshot2.id() + "\n");
            writer.write("- **Date:** " + dateFormat.format(new Date(snapshot2.timestamp())) + "\n\n");
            
            writer.write("## Comparison\n\n");
            writer.write("| Metric | Snapshot 1 | Snapshot 2 | Difference |\n");
            writer.write("|--------|------------|------------|------------|\n");
            writer.write(formatComparison("TPS", snapshot1.tps(), snapshot2.tps(), "%.2f"));
            writer.write(formatComparison("MSPT", snapshot1.mspt(), snapshot2.mspt(), "%.2f"));
            writer.write(formatComparison("CPU Usage", snapshot1.cpuUsage(), snapshot2.cpuUsage(), "%.2f%%"));
            writer.write(formatComparison("Player Count", snapshot1.playerCount(), snapshot2.playerCount(), "%d"));
            writer.write(formatComparison("Tile Entities", snapshot1.totalTileEntities(), snapshot2.totalTileEntities(), "%d"));
            writer.write(formatComparison("Chunks", snapshot1.totalChunks(), snapshot2.totalChunks(), "%d"));
            writer.write(formatComparison("Redstone Components", snapshot1.totalRedstoneComponents(), snapshot2.totalRedstoneComponents(), "%d"));
            writer.write(formatComparison("Average Ping", snapshot1.averagePing(), snapshot2.averagePing(), "%d"));
        }
        
        return file;
    }

    private String formatComparison(String metric, double val1, double val2, String format) {
        double diff = val2 - val1;
        String diffStr = diff > 0 ? String.format("+%s", String.format(format, diff)) : String.format(format, diff);
        return String.format("| %s | %s | %s | %s |\n", 
            metric, String.format(format, val1), String.format(format, val2), diffStr);
    }

    private String formatBytes(long bytes) {
        return ProfilerManager.formatBytes(bytes);
    }
}
