package net.rainbowfurry.minecraftServerProfiler;

import org.bukkit.plugin.java.JavaPlugin;

public class ConfigValidator {
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private boolean valid = true;

    public ConfigValidator(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
    }

    public boolean validate() {
        valid = true;
        validateTpsThresholds();
        validateMsptThresholds();
        validatePercentageThresholds();
        validatePlayersThresholds();
        validateTileEntitiesThresholds();
        validateChunksThresholds();
        validateRedstoneThresholds();
        validatePingThresholds();
        validateMemoryThresholds();
        validateHistorySettings();
        validateGuiSettings();
        return valid;
    }

    private void validateTpsThresholds() {
        double good = configManager.getTpsThresholdGood();
        double moderate = configManager.getTpsThresholdModerate();
        double bad = configManager.getTpsThresholdBad();

        if (good > 20) {
            error("TPS Threshold Good (" + good + ") cannot be greater than 20");
            valid = false;
        }
        if (moderate > 20) {
            error("TPS Threshold Moderate (" + moderate + ") cannot be greater than 20");
            valid = false;
        }
        if (bad > 20) {
            error("TPS Threshold Bad (" + bad + ") cannot be greater than 20");
            valid = false;
        }
        if (!(good > moderate && moderate > bad)) {
            error("TPS thresholds must be in order: good > moderate > bad");
            valid = false;
        }
    }

    private void validateMsptThresholds() {
        double good = configManager.getMsptThresholdGood();
        double moderate = configManager.getMsptThresholdModerate();
        double bad = configManager.getMsptThresholdBad();

        if (good < 0) {
            error("MSPT Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("MSPT Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("MSPT Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("MSPT thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validatePercentageThresholds() {
        double good = configManager.getPercentageThresholdGood();
        double moderate = configManager.getPercentageThresholdModerate();
        double bad = configManager.getPercentageThresholdBad();

        if (good < 0 || good > 100) {
            error("Percentage Threshold Good (" + good + ") must be between 0 and 100");
            valid = false;
        }
        if (moderate < 0 || moderate > 100) {
            error("Percentage Threshold Moderate (" + moderate + ") must be between 0 and 100");
            valid = false;
        }
        if (bad < 0 || bad > 100) {
            error("Percentage Threshold Bad (" + bad + ") must be between 0 and 100");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Percentage thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validatePlayersThresholds() {
        int good = configManager.getPlayersThresholdGood();
        int moderate = configManager.getPlayersThresholdModerate();
        int bad = configManager.getPlayersThresholdBad();

        if (good < 0) {
            error("Players Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("Players Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("Players Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Players thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validateTileEntitiesThresholds() {
        int good = configManager.getTileEntitiesThresholdGood();
        int moderate = configManager.getTileEntitiesThresholdModerate();
        int bad = configManager.getTileEntitiesThresholdBad();

        if (good < 0) {
            error("Tile Entities Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("Tile Entities Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("Tile Entities Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Tile Entities thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validateChunksThresholds() {
        int good = configManager.getChunksThresholdGood();
        int moderate = configManager.getChunksThresholdModerate();
        int bad = configManager.getChunksThresholdBad();

        if (good < 0) {
            error("Chunks Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("Chunks Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("Chunks Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Chunks thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validateRedstoneThresholds() {
        int good = configManager.getRedstoneThresholdGood();
        int moderate = configManager.getRedstoneThresholdModerate();
        int bad = configManager.getRedstoneThresholdBad();

        if (good < 0) {
            error("Redstone Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("Redstone Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("Redstone Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Redstone thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validatePingThresholds() {
        int good = configManager.getPingThresholdGood();
        int moderate = configManager.getPingThresholdModerate();
        int bad = configManager.getPingThresholdBad();

        if (good < 0) {
            error("Ping Threshold Good (" + good + ") cannot be negative");
            valid = false;
        }
        if (moderate < 0) {
            error("Ping Threshold Moderate (" + moderate + ") cannot be negative");
            valid = false;
        }
        if (bad < 0) {
            error("Ping Threshold Bad (" + bad + ") cannot be negative");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Ping thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validateMemoryThresholds() {
        double good = configManager.getMemoryThresholdGood();
        double moderate = configManager.getMemoryThresholdModerate();
        double bad = configManager.getMemoryThresholdBad();

        if (good < 0 || good > 100) {
            error("Memory Threshold Good (" + good + ") must be between 0 and 100");
            valid = false;
        }
        if (moderate < 0 || moderate > 100) {
            error("Memory Threshold Moderate (" + moderate + ") must be between 0 and 100");
            valid = false;
        }
        if (bad < 0 || bad > 100) {
            error("Memory Threshold Bad (" + bad + ") must be between 0 and 100");
            valid = false;
        }
        if (!(good < moderate && moderate < bad)) {
            error("Memory thresholds must be in order: good < moderate < bad");
            valid = false;
        }
    }

    private void validateHistorySettings() {
        int interval = configManager.getHistoryIntervalSeconds();
        int maxSnapshots = configManager.getMaxSnapshots();

        if (interval <= 0) {
            error("History interval-seconds (" + interval + ") must be positive");
            valid = false;
        }
        if (maxSnapshots <= 0) {
            error("History max-snapshots (" + maxSnapshots + ") must be positive");
            valid = false;
        }
    }

    private void validateGuiSettings() {
        int size = plugin.getConfig().getInt("gui.size", 54);
        if (size <= 0 || size > 54 || size % 9 != 0) {
            error("GUI size (" + size + ") must be a multiple of 9 and at most 54");
            valid = false;
        }
    }

    private void error(String message) {
        plugin.getLogger().severe("[ConfigValidator] " + message);
    }
}
