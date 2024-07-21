package me.unknkriod.freelanceplug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Instrument;
import org.bukkit.Material;
import org.bukkit.Note;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

public class InventoryEvents implements Listener {
    private final Main main;
    private final FileConfiguration questsConfig;
    private final GUIManager gm;
    public InventoryEvents(Main main, FileConfiguration questsConfig, GUIManager guiManager) {
        this.main = main;
        this.questsConfig = questsConfig;
        this.gm = guiManager;
    }
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !clickedInventory.equals(event.getView().getTopInventory()) || !event.getView().getTitle().equalsIgnoreCase("Задания")) {
            return; // Проверяем, что клик был в GUI заданий
        }

        event.setCancelled(true); // Отменяем стандартное действие

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return; // Проверяем, что клик был по предмету
        }

        String questName = clickedItem.getItemMeta().getDisplayName();
        if (questName.isEmpty()) {
            return; // Проверяем, что это задание
        }

        Quest quest = Quest.getQuest(questName, questsConfig);
        // Получаем информацию о задании из конфигурации
        List<String> requiredItems = quest.reqItems();
        List<String> reward = quest.reward();
        String author = quest.author();
        String accepter = quest.accepter();
        // Левый клик - принятие задания
        if (!author.equalsIgnoreCase(player.getName())) {
            // Проверяем, что автор не пытается принять свое собственное задание
            if (accepter == null) {
                quest.accepter(player.getName());
                main.saveQuestsConfig();
                player.closeInventory();
                player.sendMessage(Component.text("Вы взяли квест \"" + questName + "\". Необходимо сдать ресурсы: " + requiredItems, NamedTextColor.GREEN, TextDecoration.BOLD));
            } else {
                if (!accepter.equalsIgnoreCase(player.getName()))
                    player.sendMessage(Component.text("Этот квест принял другой игрок!", NamedTextColor.YELLOW));
            }
        } else {
            // Если автор нажал на свое задание, удаляем его
            quest.delete();
            main.saveQuestsConfig();
            player.sendMessage(Component.text("Квест \"" + questName + "\" удалён", NamedTextColor.RED, TextDecoration.BOLD));
            player.closeInventory();
        }
        if (accepter != null && accepter.equalsIgnoreCase(player.getName())) {
            // сдача задания
            if (Main.hasRequiredItems(player, requiredItems)) {
                Main.removeRequiredItems(player, requiredItems);
                Main.giveReward(player, reward);
                quest.delete();
                main.saveQuestsConfig();
                player.sendActionBar(Component.text("Квест выполнен!", NamedTextColor.GREEN));
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                player.closeInventory();
            } else {
                player.sendActionBar(Component.text("Недостаточно ресурсов для сдачи квеста", NamedTextColor.RED));
                player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.C));
            }
        }
    }
}