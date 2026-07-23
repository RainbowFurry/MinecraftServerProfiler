package net.rainbowfurry.minecraftServerProfiler;

public record Alert(
        String name,
        boolean enabled,
        String type,
        double threshold,
        String message,
        boolean broadcastToOps,
        boolean broadcastToConsole
) {}
