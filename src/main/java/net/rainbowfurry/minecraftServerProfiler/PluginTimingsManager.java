package net.rainbowfurry.minecraftServerProfiler;

import com.destroystokyo.paper.event.server.ServerTickEndEvent;
import com.destroystokyo.paper.event.server.ServerTickStartEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PluginTimingsManager implements Listener {
    private final MinecraftServerProfiler plugin;
    private final Map<String, Long> pluginTickTimes = new ConcurrentHashMap<>();
    private long currentTickStartTime = 0;
    private final Map<String, Long> lastPluginTimes = new ConcurrentHashMap<>();

    public PluginTimingsManager(MinecraftServerProfiler plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickStart(ServerTickStartEvent event) {
        currentTickStartTime = System.currentTimeMillis();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            lastPluginTimes.put(p.getName(), currentTickStartTime);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onServerTickEnd(ServerTickEndEvent event) {
        long tickEndTime = System.currentTimeMillis();
        long totalTickDuration = tickEndTime - currentTickStartTime;
        
        Map<String, Long> currentPluginDurations = new HashMap<>();
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            String name = p.getName();
            long duration = tickEndTime - lastPluginTimes.getOrDefault(name, currentTickStartTime);
            currentPluginDurations.put(name, duration);
        }
        
        long trackedDuration = currentPluginDurations.values().stream().mapToLong(Long::longValue).sum();
        long proportionalFactor = trackedDuration > 0 ? totalTickDuration : 0;
        
        for (Map.Entry<String, Long> entry : currentPluginDurations.entrySet()) {
            String pluginName = entry.getKey();
            long duration = entry.getValue();
            
            long proportionalDuration = 0;
            if (trackedDuration > 0) {
                proportionalDuration = (duration * proportionalFactor) / trackedDuration;
            }
            
            pluginTickTimes.merge(pluginName, proportionalDuration, Long::sum);
        }
    }

    public List<PluginTiming> getTopPlugins(int limit) {
        return pluginTickTimes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> new PluginTiming(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<PluginTiming> getAllPluginsSorted() {
        return pluginTickTimes.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(entry -> new PluginTiming(entry.getKey(), entry.getValue()))
                .toList();
    }

    public void resetTimings() {
        pluginTickTimes.clear();
        lastPluginTimes.clear();
    }

    public long getTotalTimeForPlugin(String pluginName) {
        return pluginTickTimes.getOrDefault(pluginName, 0L);
    }
}
