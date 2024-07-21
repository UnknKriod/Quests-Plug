package me.unknkriod.freelanceplug;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CommandTabComplete implements TabCompleter {
    private final FileConfiguration questsConfig;
    public CommandTabComplete(FileConfiguration questsConfig) {
        this.questsConfig = questsConfig;
    }
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("add");
            if (sender.hasPermission("freelance.admin")) completions.add("setAuthor");
        } else if (args[0].equalsIgnoreCase("setAuthor")) {
            if (args.length == 2) {
                if (questsConfig.isConfigurationSection("quests")) {
                    completions.addAll(questsConfig.getConfigurationSection("quests").getKeys(false));
                }
            } else if (args.length == 3) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    completions.add(p.getName());
                }
            }
        }

        List<String> result = new ArrayList<>();
        String argument = args[args.length - 1];

        for (String suggestion : completions) {
            if (suggestion.toLowerCase().startsWith(argument.toLowerCase())) {
                result.add(suggestion);
            }
        }

        return result;
    }
}