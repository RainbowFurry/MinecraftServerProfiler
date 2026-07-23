package net.rainbowfurry.minecraftServerProfiler;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class MinecraftServerProfiler extends JavaPlugin {

    private ProfilerManager profilerManager;
    private ConfigManager configManager;
    private HistoryManager historyManager;
    private PluginTimingsManager pluginTimingsManager;
    private GuiManager guiManager;
    private AlertManager alertManager;
    private ExportManager exportManager;
    private UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configManager = new ConfigManager(this);
        ConfigValidator validator = new ConfigValidator(this, configManager);
        if (!validator.validate()) {
            getLogger().severe("Config validation failed! Using default values.");
        }
        this.pluginTimingsManager = new PluginTimingsManager(this);
        this.profilerManager = new ProfilerManager(this, pluginTimingsManager);
        this.historyManager = new HistoryManager(this, configManager);
        this.exportManager = new ExportManager(this, configManager);
        this.alertManager = new AlertManager(this, profilerManager, configManager);
        this.guiManager = new GuiManager(this, configManager, profilerManager);
        this.updateChecker = new UpdateChecker(this, configManager);
        ProfilerCommand profilerCommand = new ProfilerCommand(profilerManager, configManager, guiManager, historyManager, exportManager, this);
        Objects.requireNonNull(this.getCommand("profiler")).setExecutor(profilerCommand);
        Objects.requireNonNull(this.getCommand("profiler")).setTabCompleter(profilerCommand);
        Bukkit.getPluginManager().registerEvents(pluginTimingsManager, this);
        Bukkit.getPluginManager().registerEvents(guiManager, this);
        Bukkit.getPluginManager().registerEvents(updateChecker, this);
        getLogger().info("Server Profiler enabled!");
    }
    
    public ExportManager getExportManager() {
        return exportManager;
    }
    
    public PluginTimingsManager getPluginTimingsManager() {
        return pluginTimingsManager;
    }

    @Override
    public void onDisable() {
        if (historyManager != null) {
            historyManager.close();
        }
        getLogger().info("Server Profiler disabled!");
    }

    public ProfilerManager getProfilerManager() {
        return profilerManager;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }
    
    public GuiManager getGuiManager() {
        return guiManager;
    }

    public UpdateChecker getUpdateChecker() {
        return updateChecker;
    }
}
