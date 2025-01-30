package com.fread.cloverautosave;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class AutoSaveCommand implements CommandExecutor {

    private final CloverAutoSave plugin;

    public AutoSaveCommand(CloverAutoSave plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("§aCloverAutoSave v" + plugin.getDescription().getVersion());
            sender.sendMessage("§aИспользуйте /autosave reload для перезагрузки конфигурации или /autosave check для проверки обновлений.");
            return true;
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                plugin.reloadPluginConfig();
                sender.sendMessage("§aКонфигурация CloverAutoSave перезагружена.");
                plugin.getLogger().info("Конфигурация CloverAutoSave перезагружена по команде.");
                return true;
            } else if (args[0].equalsIgnoreCase("check")) {
                if (plugin.updateChecker != null) {
                    plugin.updateChecker.checkForUpdates();
                    sender.sendMessage("§aПроверка обновлений запущена. Результат будет выведен в консоль.");
                } else {
                    sender.sendMessage("§cПроверка обновлений не настроена или отключена.");
                }
                return true;
            }
        }

        sender.sendMessage("§cНеверный аргумент. Используйте /autosave reload или /autosave check.");
        return false;
    }
}
