package de.breakcraft.lobby;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;

public class ChooseInventoryHolder implements InventoryHolder {
    private final Inventory inventory;
    
    public ChooseInventoryHolder() {
        inventory = Bukkit.createInventory(this, 3*9, "§5Serverauswahl : Breakcraft");
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        glass.setItemMeta(meta);
        ItemStack grass = new ItemStack(Material.OAK_LEAVES);
        meta = grass.getItemMeta();
        meta.setDisplayName("§aSurvival");
        List<String> lore = Arrays.asList(
                "",
                "§aSpiele mit deinen Freunden in einer ",
                "§anormalen Minecraft Welt",
                "",
                "§aAktuelle Minecraft-Version: §e1.21.1",
                "§cBeitritt nur mit mind. 1.21 möglich !"
        );
        meta.setLore(lore);
        grass.setItemMeta(meta);

        ItemStack sapling = new ItemStack(Material.DIAMOND_SWORD);
        meta = sapling.getItemMeta();
        meta.setDisplayName("§aChallenges");
        meta.addEnchant(Enchantment.FIRE_ASPECT, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        lore = Arrays.asList(
                "",
                "§aSpiele Minecraft mit deutlichen Erschwerungen durch !",
                ChatColor.RED + "Derzeit nicht verfügbar !"
        );
        meta.setLore(lore);
        sapling.setItemMeta(meta);

        for(int i = 0; i < 9; i++) {
            inventory.setItem(i, glass);
        }

        inventory.setItem(9, glass);
        inventory.setItem(17, glass);

        inventory.setItem(10, grass);
        inventory.setItem(11, sapling);

        for(int i = 18; i < inventory.getSize(); i++) {
            inventory.setItem(i, glass);
        }
    }
    
    @Override
    public Inventory getInventory() {
        return inventory;
    }
}
