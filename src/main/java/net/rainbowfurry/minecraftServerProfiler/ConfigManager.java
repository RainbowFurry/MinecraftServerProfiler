package net.rainbowfurry.minecraftServerProfiler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    private final MinecraftServerProfiler plugin;

    public ConfigManager(MinecraftServerProfiler plugin) {
        this.plugin = plugin;
    }

    // Status Colors
    public String getStatusColorGood() { return plugin.getConfig().getString("status-colors.good", "<green>"); }
    public String getStatusColorModerate() { return plugin.getConfig().getString("status-colors.moderate", "<yellow>"); }
    public String getStatusColorBad() { return plugin.getConfig().getString("status-colors.bad", "<gold>"); }
    public String getStatusColorCritical() { return plugin.getConfig().getString("status-colors.critical", "<red>"); }

    // Text Colors
    public String getTextColorLabel() { return plugin.getConfig().getString("text-colors.label", "<dark_gray>"); }
    public String getTextColorHighlight() { return plugin.getConfig().getString("text-colors.highlight", "<aqua>"); }
    public String getTextColorNone() { return plugin.getConfig().getString("text-colors.none", "<gray>"); }

    // Gradients
    public String getGradientHeader() { return plugin.getConfig().getString("gradients.header", "<gradient:#1a73e8:#34a853>"); }
    public String getGradientSystem() { return plugin.getConfig().getString("gradients.system", "<gradient:#1a73e8:#34a853>"); }
    public String getGradientGame() { return plugin.getConfig().getString("gradients.game", "<gradient:#34a853:#fbbc05>"); }
    public String getGradientWorlds() { return plugin.getConfig().getString("gradients.worlds", "<gradient:#fbbc05:#ea4335>"); }
    public String getGradientBreakdown() { return plugin.getConfig().getString("gradients.breakdown", "<gradient:#ea4335:#1a73e8>"); }
    public String getGradientTop() { return plugin.getConfig().getString("gradients.top", "<gradient:#fbbc05:#ea4335>"); }

    // TPS Thresholds
    public double getTpsThresholdGood() { return plugin.getConfig().getDouble("thresholds.tps.good", 19.0); }
    public double getTpsThresholdModerate() { return plugin.getConfig().getDouble("thresholds.tps.moderate", 15.0); }
    public double getTpsThresholdBad() { return plugin.getConfig().getDouble("thresholds.tps.bad", 10.0); }

    // MSPT Thresholds
    public double getMsptThresholdGood() { return plugin.getConfig().getDouble("thresholds.mspt.good", 30.0); }
    public double getMsptThresholdModerate() { return plugin.getConfig().getDouble("thresholds.mspt.moderate", 40.0); }
    public double getMsptThresholdBad() { return plugin.getConfig().getDouble("thresholds.mspt.bad", 50.0); }

    // Percentage Thresholds
    public double getPercentageThresholdGood() { return plugin.getConfig().getDouble("thresholds.percentage.good", 10.0); }
    public double getPercentageThresholdModerate() { return plugin.getConfig().getDouble("thresholds.percentage.moderate", 20.0); }
    public double getPercentageThresholdBad() { return plugin.getConfig().getDouble("thresholds.percentage.bad", 30.0); }

    // Players Thresholds
    public int getPlayersThresholdGood() { return plugin.getConfig().getInt("thresholds.players.good", 50); }
    public int getPlayersThresholdModerate() { return plugin.getConfig().getInt("thresholds.players.moderate", 100); }
    public int getPlayersThresholdBad() { return plugin.getConfig().getInt("thresholds.players.bad", 200); }

    // Tile Entities Thresholds
    public int getTileEntitiesThresholdGood() { return plugin.getConfig().getInt("thresholds.tile-entities.good", 1000); }
    public int getTileEntitiesThresholdModerate() { return plugin.getConfig().getInt("thresholds.tile-entities.moderate", 5000); }
    public int getTileEntitiesThresholdBad() { return plugin.getConfig().getInt("thresholds.tile-entities.bad", 20000); }

    // Chunks Thresholds
    public int getChunksThresholdGood() { return plugin.getConfig().getInt("thresholds.chunks.good", 500); }
    public int getChunksThresholdModerate() { return plugin.getConfig().getInt("thresholds.chunks.moderate", 1500); }
    public int getChunksThresholdBad() { return plugin.getConfig().getInt("thresholds.chunks.bad", 5000); }

    // Redstone Thresholds
    public int getRedstoneThresholdGood() { return plugin.getConfig().getInt("thresholds.redstone.good", 500); }
    public int getRedstoneThresholdModerate() { return plugin.getConfig().getInt("thresholds.redstone.moderate", 2000); }
    public int getRedstoneThresholdBad() { return plugin.getConfig().getInt("thresholds.redstone.bad", 5000); }

    // Ping Thresholds
    public int getPingThresholdGood() { return plugin.getConfig().getInt("thresholds.ping.good", 50); }
    public int getPingThresholdModerate() { return plugin.getConfig().getInt("thresholds.ping.moderate", 100); }
    public int getPingThresholdBad() { return plugin.getConfig().getInt("thresholds.ping.bad", 200); }

    // Memory Thresholds
    public double getMemoryThresholdGood() { return plugin.getConfig().getDouble("thresholds.memory.good", 50); }
    public double getMemoryThresholdModerate() { return plugin.getConfig().getDouble("thresholds.memory.moderate", 75); }
    public double getMemoryThresholdBad() { return plugin.getConfig().getDouble("thresholds.memory.bad", 90); }

    // GUI Settings
    public boolean isGuiEnabled() { return plugin.getConfig().getBoolean("gui.enabled", true); }
    public String getGuiTitle() { return plugin.getConfig().getString("gui.title", "<gradient:#1a73e8:#34a853><bold>Server Profiler</bold></gradient>"); }
    public int getGuiSize() { return plugin.getConfig().getInt("gui.size", 54); }

    // History settings
    public boolean isHistoryEnabled() { return plugin.getConfig().getBoolean("history.enabled", true); }
    public int getHistoryIntervalSeconds() { return plugin.getConfig().getInt("history.interval-seconds", 30); }
    public int getMaxSnapshots() { return plugin.getConfig().getInt("history.max-snapshots", 100); }
    public String getHistoryDatabaseType() { return plugin.getConfig().getString("history.database-type", "sqlite"); }
    public String getHistoryDatabaseFile() { return plugin.getConfig().getString("history.database-file", "history.db"); }

    // Alerts settings
    public boolean isAlertsEnabled() { return plugin.getConfig().getBoolean("alerts.enabled", true); }
    public List<Alert> getAlerts() {
        List<Alert> alerts = new ArrayList<>();
        List<Map<?, ?>> alertList = plugin.getConfig().getMapList("alerts.list");
        for (Map<?, ?> map : alertList) {
            alerts.add(new Alert(
                (String) map.get("name"),
                getBooleanValue(map.get("enabled"), true),
                (String) map.get("type"),
                getDoubleValue(map.get("threshold"), 0.0),
                (String) map.get("message"),
                getBooleanValue(map.get("broadcast-to-ops"), true),
                getBooleanValue(map.get("broadcast-to-console"), true)
            ));
        }
        return alerts;
    }

    private boolean getBooleanValue(Object obj, boolean defaultValue) {
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof String) {
            return Boolean.parseBoolean((String) obj);
        }
        return defaultValue;
    }

    private double getDoubleValue(Object obj, double defaultValue) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        if (obj instanceof String) {
            try {
                return Double.parseDouble((String) obj);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    public boolean isDiscordEnabled() { return plugin.getConfig().getBoolean("alerts.discord.enabled", false); }
    public String getDiscordWebhookUrl() { return plugin.getConfig().getString("alerts.discord.webhook-url", ""); }
    public String getDiscordUsername() { return plugin.getConfig().getString("alerts.discord.username", "ServerProfiler"); }
    public String getDiscordAvatarUrl() { return plugin.getConfig().getString("alerts.discord.avatar-url", ""); }
    public String getDiscordEmbedColor() { return plugin.getConfig().getString("alerts.discord.embed-color", "#ff0000"); }

    // Update check settings
    public boolean isUpdateCheckEnabled() { return plugin.getConfig().getBoolean("update-check.enabled", true); }
    public boolean isCheckOnStartup() { return plugin.getConfig().getBoolean("update-check.check-on-startup", true); }
    public boolean isNotifyOpsOnJoin() { return plugin.getConfig().getBoolean("update-check.notify-ops-on-join", true); }
    public String getUpdateRepoOwner() { return plugin.getConfig().getString("update-check.repo-owner", "YOUR-USERNAME"); }
    public String getUpdateRepoName() { return plugin.getConfig().getString("update-check.repo-name", "MinecraftServerProfiler"); }

    // Export settings
    public boolean isExportEnabled() { return plugin.getConfig().getBoolean("export.enabled", true); }
    public String getExportDefaultFormat() { return plugin.getConfig().getString("export.default-format", "json"); }
    public String getExportDirectory() { return plugin.getConfig().getString("export.directory", "exports"); }
}
