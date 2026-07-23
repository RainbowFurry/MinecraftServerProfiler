package net.rainbowfurry.minecraftServerProfiler;

import java.util.Map;

public record Snapshot(
    long id,
    long timestamp,
    double tps,
    double mspt,
    double cpuUsage,
    long memoryUsed,
    long memoryMax,
    int playerCount,
    int totalTileEntities,
    int totalChunks,
    String worldChunkData,
    int totalRedstoneComponents,
    int averagePing,
    String categoriesData,
    String topEntityData,
    String topChunkData,
    String topPluginData,
    String worldData,
    String entityTypeCountsData,
    String tileEntityTypeCountsData
) {
}
