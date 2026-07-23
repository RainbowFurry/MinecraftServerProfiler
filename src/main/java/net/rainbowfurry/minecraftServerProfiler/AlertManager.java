package net.rainbowfurry.minecraftServerProfiler;

import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class AlertManager {
    private final MinecraftServerProfiler plugin;
    private final ProfilerManager profilerManager;
    private final ConfigManager configManager;
    private final HttpClient httpClient;
    private final MiniMessage miniMessage;
    private final Map<String, Long> lastAlertTimes;

    public AlertManager(MinecraftServerProfiler plugin, ProfilerManager profilerManager, ConfigManager configManager) {
        this.plugin = plugin;
        this.profilerManager = profilerManager;
        this.configManager = configManager;
        this.httpClient = HttpClient.newHttpClient();
        this.miniMessage = MiniMessage.miniMessage();
        this.lastAlertTimes = new HashMap<>();
        startChecking();
    }

    private void startChecking() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!configManager.isAlertsEnabled()) return;
                checkAlerts();
            }
        }.runTaskTimer(plugin, 20L, 20L * 5);
    }

    private void checkAlerts() {
        for (Alert alert : configManager.getAlerts()) {
            if (!alert.enabled()) continue;

            double value = 0;
            boolean trigger = false;

            switch (alert.type()) {
                case "tps-below":
                    value = profilerManager.getTPS();
                    trigger = value < alert.threshold();
                    break;
                case "mspt-above":
                    value = profilerManager.getMSPT();
                    trigger = value > alert.threshold();
                    break;
                case "memory-above-percent":
                    var heap = profilerManager.getHeapMemoryUsage();
                    value = (double) heap.getUsed() / heap.getMax() * 100;
                    trigger = value > alert.threshold();
                    break;
            }

            if (trigger) {
                handleAlert(alert, value);
            }
        }
    }

    private void handleAlert(Alert alert, double value) {
        long now = System.currentTimeMillis();
        long cooldown = 30000;
        if (lastAlertTimes.containsKey(alert.name()) && now - lastAlertTimes.get(alert.name()) < cooldown) {
            return;
        }
        lastAlertTimes.put(alert.name(), now);

        String formattedMessage = alert.message()
                .replace("<threshold>", String.valueOf(alert.threshold()))
                .replace("<value>", String.format("%.2f", value));

        if (alert.broadcastToConsole()) {
            plugin.getServer().getConsoleSender().sendMessage(miniMessage.deserialize(formattedMessage));
        }

        if (alert.broadcastToOps()) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.sendMessage(miniMessage.deserialize(formattedMessage));
                }
            }
        }

        if (configManager.isDiscordEnabled()) {
            sendToDiscord(alert, formattedMessage);
        }
    }

    private void sendToDiscord(Alert alert, String message) {
        String webhookUrl = configManager.getDiscordWebhookUrl();
        if (webhookUrl.isEmpty()) return;

        String embedColorStr = configManager.getDiscordEmbedColor().replace("#", "");
        int embedColor = Integer.parseInt(embedColorStr, 16);

        String jsonPayload = String.format(
                "{\"username\":\"%s\",\"avatar_url\":\"%s\",\"embeds\":[{\"title\":\"%s\",\"description\":\"%s\",\"color\":%d}]}",
                escapeJson(configManager.getDiscordUsername()),
                escapeJson(configManager.getDiscordAvatarUrl()),
                escapeJson("Server Alert: " + alert.name()),
                escapeJson(message),
                embedColor
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(webhookUrl))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.discarding());
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
}