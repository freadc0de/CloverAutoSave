package com.fread.cloverautosave;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class CloverAutoSave extends JavaPlugin {

    private long saveIntervalTicks;
    private String saveSuccessMessage;
    private String enabledMessage;
    private String disabledMessage;

    // Обновления
    private boolean updateCheckEnabled;
    private long updateCheckIntervalTicks;

    public UpdateChecker updateChecker;

    @Override
    public void onEnable() {
        // Сохранение встроенного config.yml, если его нет
        saveDefaultConfig();

        // Загрузка конфигурации
        loadConfigSettings();

        // Регистрация команды
        this.getCommand("autosave").setExecutor(new AutoSaveCommand(this));

        // Вывод сообщения о включении плагина
        getLogger().info(enabledMessage);

        // Запуск задачи сохранения мира
        new BukkitRunnable() {
            @Override
            public void run() {
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    world.save();
                }
                getLogger().info(saveSuccessMessage);
            }
        }.runTaskTimer(this, saveIntervalTicks, saveIntervalTicks);

        // Инициализация и запуск проверки обновлений
        if (updateCheckEnabled) {
            String currentVersion = this.getDescription().getVersion();
            updateChecker = new UpdateChecker(this, currentVersion);
            updateChecker.checkForUpdates();

            // Запланировать последующие проверки обновлений
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateChecker.checkForUpdates();
                }
            }.runTaskTimerAsynchronously(this, updateCheckIntervalTicks, updateCheckIntervalTicks);
        }
    }

    @Override
    public void onDisable() {
        getLogger().info(disabledMessage);
    }

    /**
     * Метод для загрузки настроек из конфигурационного файла.
     */
    public void loadConfigSettings() {
        FileConfiguration config = getConfig();

        // Загрузка интервала сохранения и конвертация в тики
        int intervalSeconds = config.getInt("save-interval-seconds", 60);
        saveIntervalTicks = intervalSeconds * 20L; // 20 тиков = 1 секунда

        // Загрузка сообщений из конфигурации
        saveSuccessMessage = config.getString("save-success-message", "Миры успешно сохранены.");
        enabledMessage = config.getString("enabled-message", "CloverAutoSave включен!");
        disabledMessage = config.getString("disabled-message", "CloverAutoSave отключен.");

        // Загрузка настроек обновлений
        if (config.isConfigurationSection("update-check")) {
            updateCheckEnabled = config.getBoolean("update-check.enabled", true);
            int checkIntervalHours = config.getInt("update-check.check-interval-hours", 24);
            updateCheckIntervalTicks = checkIntervalHours * 60 * 60 * 20L; // часы -> тики
        } else {
            updateCheckEnabled = false;
        }
    }

    /**
     * Перезагрузка настроек из конфигурации.
     */
    public void reloadPluginConfig() {
        reloadConfig();
        loadConfigSettings();

        // Если проверка обновлений включена и UpdateChecker не инициализирован, создать новый
        if (updateCheckEnabled && updateChecker == null) {
            String currentVersion = this.getDescription().getVersion();
            updateChecker = new UpdateChecker(this, currentVersion);
            updateChecker.checkForUpdates();

            // Запланировать последующие проверки обновлений
            new BukkitRunnable() {
                @Override
                public void run() {
                    updateChecker.checkForUpdates();
                }
            }.runTaskTimerAsynchronously(this, updateCheckIntervalTicks, updateCheckIntervalTicks);
        }
    }
}