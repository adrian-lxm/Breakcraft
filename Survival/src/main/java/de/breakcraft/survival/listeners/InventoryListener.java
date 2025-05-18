package de.breakcraft.survival.listeners;

import de.breakcraft.survival.pawnshop.PawnshopHolder;
import de.breakcraft.survival.pawnshop.PawnshopManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player p)) return;
        var clickedInv = e.getClickedInventory();
        if(clickedInv == null) return;

        if(clickedInv.getHolder() == null || !(clickedInv.getHolder() instanceof PawnshopHolder)) return;

        e.setCancelled(true);
        if(e.getCurrentItem() == null) return;
        var type = e.getCurrentItem().getType();
        if(type == Material.GRAY_STAINED_GLASS_PANE || type == Material.CRAFTING_TABLE) return;
        PawnshopManager.handleEvent(p, e.isRightClick(), e.getCurrentItem());
    }


}
