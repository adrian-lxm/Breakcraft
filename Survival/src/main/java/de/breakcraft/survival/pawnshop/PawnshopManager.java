package de.breakcraft.survival.pawnshop;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class PawnshopManager {

    public static void handleEvent(Player p, boolean slot, ItemStack item) {
        if(slot) {
            PawnShopItem item1 = manager.getPawnShopItemByMaterial(item.getType());
            if(p.getInventory().contains(item1.getMaterial())) {
                int slot2 = 0;
                for(int i = 0; i < p.getInventory().getSize(); i++) {
                    if(p.getInventory().getItem(i) != null) {
                        if(p.getInventory().getItem(i).getType() == item1.getMaterial()) {
                            slot2 = i;
                            break;
                        }
                    }
                }
                int count = p.getInventory().getItem(slot2).getAmount();
                p.getInventory().clear(slot2);
                SurvivalPlugin.getInstance().econ.depositPlayer(p, count*item1.getWorth());
                p.sendMessage("§aDu hast §e" + (count*item1.getWorth()) + " §a€ für §e" + count + "§ax " + item1.getName() + " gekriegt !");
            } else p.sendMessage("§cDu hast keine " + item1.getName() + " in deinem Inventar !");
        } else {
            PawnShopItem item1 = manager.getPawnShopItemByMaterial(item.getType());
            if(p.getInventory().contains(item1.getMaterial())) {
                int slot2 = 0;
                for(int i = 0; i < p.getInventory().getSize(); i++) {
                    if(p.getInventory().getItem(i) != null) {
                        if(p.getInventory().getItem(i).getType() == item1.getMaterial()) {
                            slot2 = i;
                            break;
                        }
                    }
                }
                if(p.getInventory().getItem(slot2).getAmount() == 1) {
                    p.getInventory().clear(slot2);
                    SurvivalPlugin.getInstance().econ.depositPlayer(p, item1.getWorth());
                    p.sendMessage("§aDu hast §e" + item1.getWorth() + " §a€ für §e1§ax " + item1.getName() + " gekriegt !");
                } else {
                    int newAmount = p.getInventory().getItem(slot2).getAmount() - 1;
                    p.getInventory().getItem(slot2).setAmount(newAmount);
                    SurvivalPlugin.getInstance().econ.depositPlayer(p, item1.getWorth());
                    p.sendMessage("§aDu hast §e" + item1.getWorth() + " §a€ für §e1§ax " + item1.getName() + " gekriegt !");
                }
            } else p.sendMessage("§cDu hast keine " + item1.getName() + " in deinem Inventar !");
        }
    }

}
