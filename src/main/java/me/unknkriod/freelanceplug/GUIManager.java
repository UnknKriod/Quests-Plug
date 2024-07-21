package me.unknkriod.freelanceplug;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class GUIManager {
    private final FileConfiguration questsConfig;
    public GUIManager(FileConfiguration questsConfig) {
        this.questsConfig = questsConfig;
    }
    public void openFreelanceGUI(Player player) {
        ConfigurationSection questsSection = questsConfig.getConfigurationSection("quests");

        int guiSize = 9; // Размер по умолчанию

        if (questsSection != null && !questsSection.getKeys(false).isEmpty()) {
            int questCount = questsSection.getKeys(false).size();
            guiSize = (int) Math.ceil(questCount / 9.0) * 9; // Вычисляем размер GUI
        }

        Inventory gui = Bukkit.createInventory(player, guiSize, "Задания");

        if (questsSection != null) {
            for (String questName : questsSection.getKeys(false)) {
                ItemStack questItem = createQuestItem(questName, player);
                gui.addItem(questItem);
            }
        }

        player.openInventory(gui);
    }

    private ItemStack createQuestItem(String questName, Player p) {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        Quest quest = Quest.getQuest(questName, questsConfig);

        if (quest != null) {
            meta.setDisplayName(questName);

            List<String> lore = new ArrayList<>();
            String author = quest.author();
            String accepter = quest.accepter();
            List<String> requiredItemsList = quest.reqItems();
            List<String> rewardList = quest.reward();
            lore.add("§eАвтор: " + author);
            lore.add(" ");
            if (accepter != null) lore.add("§dПринят " + accepter);

            lore.add("§bТребуемые ресурсы: ");
            for (String requiredItem : requiredItemsList) {
                lore.add("§b  - " + requiredItem);
            }

            lore.add("§6Награда: ");
            for (String reward : rewardList) {
                lore.add("§e  - " + reward);
            }

            lore.add(" ");
            if (author.equalsIgnoreCase(p.getName())) lore.add("§cНажмите, чтобы удалить");
            else if (accepter != null && accepter.equalsIgnoreCase(p.getName())) lore.add("§aНажмите, чтобы сдать");
            else lore.add("§aНажмите, чтобы принять");

            meta.setLore(lore);

            item.setItemMeta(meta);
            return item;
        } else System.out.println("Quest is null!");
        return item;
    }
}