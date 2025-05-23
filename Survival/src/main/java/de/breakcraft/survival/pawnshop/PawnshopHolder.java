package de.breakcraft.survival.pawnshop;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class PawnshopHolder implements InventoryHolder {
    private final Inventory shopInventory;

    public PawnshopHolder() {
        shopInventory = Bukkit.createInventory(this, 6*9, "§f§5Pfandhaus");
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.setDisplayName(" ");
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        glass.setItemMeta(meta);
        ItemStack pawnshop = new ItemStack(Material.CRAFTING_TABLE);
        meta = pawnshop.getItemMeta();
        meta.setDisplayName("§aPfandhaus");
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore1 = Arrays.asList("", "§aVerkaufe deine Sachen hier gegen Ingame €");
        meta.setLore(lore1);
        pawnshop.setItemMeta(meta);
        for(int i = 0; i < 9; i++) {
            shopInventory.setItem(i, glass);
        }

        shopInventory.setItem(4, pawnshop);

        shopInventory.setItem(9, glass);
        shopInventory.setItem(17, glass);

        shopInventory.setItem(18, glass);
        shopInventory.setItem(26, glass);

        shopInventory.setItem(27, glass);
        shopInventory.setItem(35, glass);

        shopInventory.setItem(36, glass);
        shopInventory.setItem(44, glass);

        for(int i = 45; i < shopInventory.getSize(); i++) {
            shopInventory.setItem(i, glass);
        }

        List<String> lore2 = Arrays.asList("", "§aLinksklick um ein Stück zu verkaufen", "§eRechstklick um nächsten Slot zu verkaufen");

        int i = 10;
        int i2 = 17;
        for(PawnShopItem item : PawnShopItem.values()) {
            ItemStack itemstack = new ItemStack(item.getMaterial());
            meta = itemstack.getItemMeta();
            meta.setDisplayName("§a" + item.getName() + "  -  " + item.getWorth() + "€");
            meta.setLore(lore2);
            itemstack.setItemMeta(meta);
            shopInventory.setItem(i, itemstack);
            i++;
            if(i == i2) {
                i += 2;
                i2 += 9;
            }
        }
    }

    public void handleEvent(Player p, boolean stack, Material type) {
        PawnShopItem shopItem = PawnShopItem.getByMaterial(type);
        if(shopItem == null) return;
        var inventory = p.getInventory();
        if(!inventory.contains(shopItem.getMaterial())) {
            p.sendMessage("§cDu hast kein " + shopItem.getName() + " in deinem Inventar!");
            return;
        }
        int slot = 0;
        int lowest = 65; // higher than a full possible stack to catch the itemStack if lowest amount is a full stack
        for(int i = 0; i < inventory.getSize(); i++) {
            if(inventory.getItem(i) == null || inventory.getItem(i).getType() != type) continue;
            if(inventory.getItem(i).getAmount() < lowest) {
                slot = i;
                lowest = inventory.getItem(i).getAmount();
            }
        }
        if(stack) {
            inventory.clear(slot);
            SurvivalPlugin.getInstance().getEconomy().depositPlayer(p, lowest * shopItem.getWorth());
            p.sendMessage(String.format("§aDu hast §e%dx %s §afür §e%d€ §averkauft!", lowest, shopItem.getName(), lowest * shopItem.getWorth()));
        } else {
            if(lowest > 1) inventory.getItem(slot).setAmount(lowest - 1);
            else inventory.clear(slot);
            SurvivalPlugin.getInstance().getEconomy().depositPlayer(p, lowest * shopItem.getWorth());
            p.sendMessage(String.format("§aDu hast §e1x %s §afür §e%d€ §averkauft!", shopItem.getName(), shopItem.getWorth()));
        }
    }

    @Override
    public Inventory getInventory() {
        return shopInventory;
    }

}
