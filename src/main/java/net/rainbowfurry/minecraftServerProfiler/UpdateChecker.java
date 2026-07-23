package net.rainbowfurry.minecraftServerProfiler;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class UpdateChecker implements Listener {
    private final MinecraftServerProfiler plugin;
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final Gson gson;
    private final MiniMessage miniMessage;
    private String latestVersion;
    private boolean updateAvailable;

    public UpdateChecker(MinecraftServerProfiler plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
        this.miniMessage = MiniMessage.miniMessage();
        this.latestVersion = null;
        this.updateAvailable = false;

        if (configManager.isUpdateCheckEnabled() && configManager.isCheckOnStartup()) {
            checkForUpdatesAsync();
        }
    }

    public void checkForUpdatesAsync() {
        new BukkitRunnable() {
            @Override
            public void run() {
                checkForUpdates();
            }
        }.runTaskAsynchronously(plugin);
    }

    private void checkForUpdates() {
        try {
            String repoOwner = configManager.getUpdateRepoOwner();
            String repoName = configManager.getUpdateRepoName();
            String apiUrl = String.format("https://api.github.com/repos/%s/%s/releases/latest", repoOwner, repoName);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Accept", "application/vnd.github.v3+json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                JsonObject json = gson.fromJson(response.body(), JsonObject.class);
                latestVersion = json.get("tag_name").getAsString();

                String currentVersion = plugin.getDescription().getVersion();
                updateAvailable = !currentVersion.equals(latestVersion);

                if (updateAvailable) {
                    plugin.getLogger().info("A new version of MinecraftServerProfiler is available: " + latestVersion);
                    plugin.getLogger().info("Download it at: " + json.get("html_url").getAsString());
                } else {
                    plugin.getLogger().info("You are running the latest version of MinecraftServerProfiler.");
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check for updates: " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!configManager.isUpdateCheckEnabled() || !configManager.isNotifyOpsOnJoin()) {
            return;
        }

        Player player = event.getPlayer();
        if (player.isOp() && updateAvailable) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    player.sendMessage(miniMessage.deserialize("<gold>[ServerProfiler] A new version is available: <yellow>" + latestVersion));
                }
            }.runTask(plugin);
        }
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public String getLatestVersion() {
        return latestVersion;
    }
}
