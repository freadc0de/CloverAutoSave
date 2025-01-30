package com.fread.cloverautosave;

import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class UpdateChecker {
    private final JavaPlugin plugin;
    private final String repo;
    private final String currentVersion;

    public UpdateChecker(JavaPlugin plugin, String currentVersion) {
        this.plugin = plugin;
        this.repo = "freadc0de/CloverAutoSave"; // Жестко закодированный репозиторий
        this.currentVersion = currentVersion;
    }

    public CompletableFuture<String> getLatestVersionAsync() {
        String apiUrl = "https://api.github.com/repos/" + repo + "/releases/latest";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Accept", "application/vnd.github.v3+json")
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        plugin.getLogger().warning("Не удалось получить информацию о последнем релизе GitHub. Статус: " + response.statusCode());
                        return null;
                    }
                    String body = response.body();
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    return json.get("tag_name").getAsString();
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Ошибка при обращении к GitHub API: " + ex.getMessage());
                    return null;
                });
    }

    public void checkForUpdates() {
        getLatestVersionAsync().thenAccept(latestVersion -> {
            if (latestVersion == null) {
                return;
            }

            String normalizedLatest = latestVersion.startsWith("v") ? latestVersion.substring(1) : latestVersion;
            String normalizedCurrent = currentVersion.startsWith("v") ? currentVersion.substring(1) : currentVersion;

            if (isNewerVersion(normalizedLatest, normalizedCurrent)) {
                plugin.getLogger().warning("§eДоступна новая версия AutoSavePlugin: " + latestVersion + " (Текущая версия: " + currentVersion + ").");
                plugin.getLogger().warning("§eСкачайте обновление здесь: https://github.com/" + repo + "/releases/latest");
            } else {
                plugin.getLogger().info("AutoSavePlugin обновлён до последней версии (" + currentVersion + ").");
            }
        });
    }

    /**
     * Сравнивает версии в формате x.y.z
     */
    private boolean isNewerVersion(String latest, String current) {
        String[] latestParts = latest.split("\\.");
        String[] currentParts = current.split("\\.");

        int length = Math.max(latestParts.length, currentParts.length);
        for (int i = 0; i < length; i++) {
            int latestPart = i < latestParts.length ? parseVersionPart(latestParts[i]) : 0;
            int currentPart = i < currentParts.length ? parseVersionPart(currentParts[i]) : 0;

            if (latestPart > currentPart) {
                return true;
            } else if (latestPart < currentPart) {
                return false;
            }
        }
        return false;
    }

    private int parseVersionPart(String part) {
        try {
            return Integer.parseInt(part);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
