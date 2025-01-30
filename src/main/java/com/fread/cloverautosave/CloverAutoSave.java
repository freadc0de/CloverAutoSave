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

    @Override
    public void onEnable() {
        // Сохранение встроенного config.yml, если его нет
        saveDefaultConfig();

        // Загрузка конфигурации
        loadConfigSettings();

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
    }

    @Override
    public void onDisable() {
        getLogger().info(disabledMessage);
    }

    /**
     * Метод для загрузки настроек из конфигурационного файла.
     */
    private void loadConfigSettings() {
        FileConfiguration config = getConfig();

        // Загрузка интервала сохранения и конвертация в тики
        int intervalSeconds = config.getInt("save-interval-seconds", 60);
        saveIntervalTicks = intervalSeconds * 20L; // 20 тиков = 1 секунда

        // Загрузка сообщений из конфигурации
        saveSuccessMessage = config.getString("save-success-message", "Миры успешно сохранены.");
        enabledMessage = config.getString("enabled-message", "AutoSavePlugin включен!");
        disabledMessage = config.getString("disabled-message", "AutoSavePlugin отключен.");
    }
}
