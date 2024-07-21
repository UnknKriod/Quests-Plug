package me.unknkriod.freelanceplug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Quest {
    private final String name;
    private final FileConfiguration questsConfig;
    private boolean builded;
    private final String path;
    public Quest(String name, FileConfiguration questsConfig) {
        this.name = name;
        this.path = "quests." + name;
        this.questsConfig = questsConfig;
    }
    public void builded(boolean b) {
        this.builded = b;
    }
    public void createSection() {
        questsConfig.createSection(path);
    }
    public String name() {
        return name;
    }
    public void author(@NotNull String author) {
        questsConfig.set(path + ".author", author);
    }
    public String author() {
        return questsConfig.getString(path + ".author");
    }
    public String accepter() {
        return questsConfig.getString(path + ".accepter");
    }
    public void accepter(String accepter) {
        questsConfig.set(path + ".accepter", accepter);
    }
    public void reqItems(List<String> prompt) {
        questsConfig.set(path + ".requiredItems", prompt);
    }
    public void reward(List<String> prompt) {
        questsConfig.set(path + ".reward", prompt);
    }
    public List<String> reqItems() {
        return questsConfig.getStringList(path + ".requiredItems");
    }
    public List<String> reward() {
        return questsConfig.getStringList(path + ".reward");
    }
    public boolean isBuilded() {
        return builded;
    }
    public static Quest getQuest(String name, FileConfiguration questsConfig) {
        String path = "quests." + name;
        if (questsConfig.contains(path)) {
            return new Quest(name, questsConfig);
        }
        return null;
    }
    public static List<Quest> getNotBuildedQuests(FileConfiguration questsConfig) {
        List<Quest> notBuildedQuests = new ArrayList<>();

        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");
        if (questsSection != null) {
            for (String questName : questsSection.getKeys(false)) {
                if (!questsConfig.contains("quests." + questName + ".requiredItems")) {
                    // Квест не содержит информацию о требуемых предметах
                    notBuildedQuests.add(new Quest(questName, questsConfig));
                }
            }
        }

        return notBuildedQuests;
    }
    public static Quest getLastNotBuildedQuest(FileConfiguration questsConfig) {
        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");
        if (questsSection != null) {
            List<String> questNames = new ArrayList<>(questsSection.getKeys(false));
            for (int i = questNames.size() - 1; i >= 0; i--) {
                String questName = questNames.get(i);
                if (!questsConfig.contains("quests." + questName + ".requiredItems") || !questsConfig.contains("quests." + questName + ".reward")) {
                    return new Quest(questName, questsConfig);
                }
            }
        }

        return null; // Если недостроенных квестов не найдено
    }
    public static List<String> getActiveQuests(Player player, FileConfiguration questsConfig) {
        List<String> activeQuests = new ArrayList<>();

        // Проходимся по всем квестам в конфигурации
        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");
        if (questsSection != null) {
            for (String questName : questsSection.getKeys(false)) {
                // Проверяем, взял ли игрок данный квест
                String author = questsConfig.getString("quests." + questName + ".author");
                if (author != null && author.equalsIgnoreCase(player.getName())) {
                    activeQuests.add(questName);
                }
            }
        }

        return activeQuests;
    }
    public void delete() {
        questsConfig.set(path, null);
        builded(false);
    }
}