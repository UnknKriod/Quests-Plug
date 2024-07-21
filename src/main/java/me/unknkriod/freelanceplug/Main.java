package me.unknkriod.freelanceplug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends JavaPlugin {
    private FileConfiguration questsConfig;
    private File questsFile;
    private final File datafolder = getDataFolder();
    @Override
    public void onEnable() {
        // Загрузка конфигурации заданий
        if (!datafolder.exists()) datafolder.mkdir();
        questsFile = new File(datafolder, "quests.yml");
        if (!questsFile.exists()) {
            saveResource("quests.yml", false);
        }
        questsConfig = YamlConfiguration.loadConfiguration(questsFile);
        GUIManager guiManager = new GUIManager(questsConfig);
        // Event Registration
        getServer().getPluginManager().registerEvents(new InventoryEvents(this, questsConfig, guiManager), this);

        // Регистрация команды
        getCommand("quests").setExecutor(new Command(guiManager, this, questsConfig));
        getCommand("quests").setTabCompleter(new CommandTabComplete(questsConfig));
    }

    @Override
    public void onDisable() {
        saveQuestsConfig();
    }

    public void addQuest(Player player, String questName, List<String> requiredItems, List<String> reward) {
        // Получаем объект Quest
        Quest quest = Quest.getQuest(questName, questsConfig);

        if (quest == null) {
            quest = new Quest(questName, questsConfig);
            // Сохраняем данные о квесте
            quest.createSection();
            quest.author(player.getName());
            quest.reqItems(requiredItems);
            quest.reward(reward);
            quest.builded(true); // Помечаем квест как построенный

            saveQuestsConfig();
            removeRequiredItems(player, reward);
            Bukkit.broadcast(MiniMessage.miniMessage().deserialize("<bold><yellow>[Задания]</yellow></bold> <player> <yellow>добавил новый квест!</yellow>", Placeholder.component("player", player.displayName())));
        } else {
            player.sendMessage(Component.text("Квест с именем \"" + questName + "\" уже существует", NamedTextColor.RED, TextDecoration.BOLD));
        }
    }
    public static List<ItemStack> parseItemStackList(String itemsString, Player player) {
        List<ItemStack> itemStackList = new ArrayList<>();
        Map<String, Integer> itemPrices = parseItemPrices(itemsString);

        for (Map.Entry<String, Integer> entry : itemPrices.entrySet()) {
            String itemName = entry.getKey();
            int amount = entry.getValue();

            Material material = Material.matchMaterial(itemName);
            if (material != null) {
                ItemStack itemStack = new ItemStack(material, amount);
                itemStackList.add(itemStack);
            } else {
                player.sendMessage(Component.text("Неизвестный материал: " + itemName, NamedTextColor.RED));
            }
        }

        return itemStackList;
    }
    public static Map<String, Integer> parseItemPrices(String input) {
        Map<String, Integer> itemPrices = new HashMap<>();
        String[] items = input.split(", "); // Предполагаем, что предметы разделены запятой и пробелом
        for (String item : items) {
            String[] parts = item.split(" - "); // Предполагаем, что название предмета и его цена разделены тире
            if (parts.length == 2) {
                String itemName = parts[0].trim();
                int price = Integer.parseInt(parts[1].trim());
                itemPrices.put(itemName, price);
            }
        }
        return itemPrices;
    }
    public void saveQuestsConfig() {
        try {
            questsConfig.save(questsFile);
        } catch (IOException e) {
            getLogger().warning("Unable to save quests.yml");
        }
    }
    public static boolean hasRequiredItems(Player player, List<String> requiredItems) {
        for (String requiredItem : requiredItems) {
            List<ItemStack> itemStackList = parseItemStackList(requiredItem, player);
            for (ItemStack requiredItemStack : itemStackList) {
                if (!player.getInventory().containsAtLeast(requiredItemStack, requiredItemStack.getAmount())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void removeRequiredItems(Player p, List<String> requiredItems) {
        for (String requiredItem : requiredItems) {
            List<ItemStack> itemStackList = parseItemStackList(requiredItem, p);
            for (ItemStack itemStack : itemStackList) {
                p.getInventory().remove(itemStack);
            }
        }
    }
    public static void giveReward(Player player, List<String> rewardList) {
        List<ItemStack> rewards = new ArrayList<>();
        for (String rewardString : rewardList) {
            List<ItemStack> items = parseItemStackList(rewardString, player);
            rewards.addAll(items);
        }
        for (ItemStack reward : rewards) {
            player.getInventory().addItem(reward);
        }
    }
    public void sendDebug(Object message) {
        getLogger().warning("[DEBUG] " + message);
    }
}