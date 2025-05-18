package de.breakcraft.lobby.Listener;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.breakcraft.lobby.LobbyPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if(e.getCurrentItem() != null && e.getCurrentItem().isSimilar(LobbyPlugin.getCompassItem())) {
            e.setCancelled(true);
            return;
        }

        if(e.getClickedInventory().getType() == InventoryType.CREATIVE || e.getClickedInventory() == null)
            return;

        if(!player.getOpenInventory().getTitle().equals("§5Serverauswahl : Breakcraft") || e.getCurrentItem() == null) return;
        e.setCancelled(true);

        if(e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        String server = e.getCurrentItem().getItemMeta().getDisplayName().substring(2);
        LobbyPlugin.getInstance().sendPluginMessage(player, "Connect", server);
        player.closeInventory();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if(e.getItemDrop().getItemStack().isSimilar(LobbyPlugin.getCompassItem())) e.setCancelled(true);
    }

}
