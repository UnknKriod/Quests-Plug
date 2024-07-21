package me.unknkriod.freelanceplug;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.awt.print.Paper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Command implements CommandExecutor {
    private final GUIManager guiManager;
    private final Main main;
    private final FileConfiguration questsConfig;
    public Command(GUIManager guiManager, Main main, FileConfiguration questsConfig) {
        this.guiManager = guiManager;
        this.main = main;
        this.questsConfig = questsConfig;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, org.bukkit.command.@NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Component.text("Комманду может выполнить только игрок!", NamedTextColor.RED));
            return true;
        }
        if (args.length < 1) {
            guiManager.openFreelanceGUI((Player) sender);
            return true;
        }
        Player p = (Player) sender;
        PlayerInventory pInv = p.getInventory();
        ItemStack nameItem = pInv.getItem(0);
        ItemStack reqItemsItem = pInv.getItem(1);
        ItemStack rewardItem = pInv.getItem(2);

        switch (args[0]) {
            case "add":
                if (nameItem != null && reqItemsItem != null && rewardItem != null) {
                    if (nameItem.getType() == Material.PAPER && reqItemsItem.getType() == Material.PAPER && rewardItem.getType() == Material.PAPER) {
                        String name = nameItem.getItemMeta().getDisplayName();
                        String reqItemsStr = reqItemsItem.getItemMeta().getDisplayName();
                        String rewardStr = rewardItem.getItemMeta().getDisplayName();
                        List<ItemStack> reqItemss = Main.parseItemStackList(reqItemsStr, p);
                        List<ItemStack> rewardd = Main.parseItemStackList(rewardStr, p);
                        if (Quest.getQuest(name, questsConfig) != null) {
                            p.sendMessage(Component.text("Это имя для задания занято!", NamedTextColor.RED));
                            return true;
                        }
                        if (reqItemss.isEmpty() || rewardd.isEmpty()) {
                            p.sendMessage(Component.text("Введите правильно название предмета и повторите попытку", NamedTextColor.RED));
                            return true;
                        }
                        List<String> reqItems = new ArrayList<>();
                        List<String> reward = new ArrayList<>();
                        Map<String, Integer> reqItemsMap = Main.parseItemPrices(reqItemsStr);
                        Map<String, Integer> rewardMap = Main.parseItemPrices(rewardStr);
                        for (Map.Entry<String, Integer> entry : reqItemsMap.entrySet()) {
                            String nam = entry.getKey();
                            int amount = entry.getValue();
                            reqItems.add(nam + " - " + amount);
                        }
                        for (Map.Entry<String, Integer> entry : rewardMap.entrySet()) {
                            String nam = entry.getKey();
                            int amount = entry.getValue();
                            reward.add(nam + " - " + amount);
                        }
                        if (Main.hasRequiredItems(p, reward)) main.addQuest(p, name, reqItems, reward);
                        else p.sendMessage(ChatColor.RED + "У вас недостаточно ресурсов! " + ChatColor.YELLOW + "Проверьте наличие награды в инвентаре");
                        break;
                    }
                    else {
                        sender.sendMessage(Component.text("Положите в 1-й слот хотбара бумагу, в названии которой указано название задания; 2-й слот - бумага с требуемыми ресурсами (название - цена) (если несколько, то название - цена, название - цена) название должно быть указано на английском языке (золотая морковь - golden_carrot); 3-й слот - бумага с наградой (название - цена) (если несколько, что название - цена, название - цена) название должно быть указано на английском язые. Убедитесь, что награда есть у вас в инвентаре", NamedTextColor.YELLOW));
                        break;
                    }
                } else {
                    sender.sendMessage(Component.text("Положите в 1-й слот хотбара бумагу, в названии которой указано название задания; 2-й слот - бумага с требуемыми ресурсами (название - цена) (если несколько, то название - цена, название - цена); 3-й слот - бумага с наградой (название - цена) (если несколько, что название - цена, название - цена). Убедитесь, что награда есть у вас в инвентаре", NamedTextColor.YELLOW));
                    break;
                }
            case "setAuthor":
                if (args.length != 3) p.sendMessage(Component.text("Использование: setAuthor <название квеста> <новый автор>", NamedTextColor.YELLOW));
                else {
                    Quest quest = Quest.getQuest(args[1], questsConfig);
                    if (quest != null) {
                        quest.author(args[2]);
                        main.saveQuestsConfig();
                        p.sendMessage(Component.text("Автор квеста " + args[1] + " установлен на " + args[2], NamedTextColor.GREEN));
                    }
                    else p.sendMessage(Component.text("Квест не найден!", NamedTextColor.RED));
                }
                break;
            default:
                sender.sendMessage("Неизвестная подкоманда");
                break;
        }

        return true;
    }
}
