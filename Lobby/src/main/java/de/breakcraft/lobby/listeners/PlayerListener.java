package de.breakcraft.lobby.listeners;

import de.breakcraft.lobby.ChooseInventoryHolder;
import de.breakcraft.lobby.LobbyPlugin;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import java.util.Arrays;
import java.util.List;

public class PlayerListener implements Listener {
    private final ItemStack compass;

    public PlayerListener() {
        compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§aServerauswahl");
        meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = Arrays.asList("", "§aDrücke Rechtsklick und sehe", "§aalle unsere Server / Gamemodis !");
        meta.setLore(lore);
        compass.setItemMeta(meta);
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.getPlayer().getInventory().setItem(0, compass);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(!(e.getPlayer().hasPermission("breakcraft.lobby.interact"))) e.setCancelled(true);
        if(!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;

        if(e.getItem() == null)
            return;

        if(!e.getItem().isSimilar(compass))
            return;

        e.getPlayer().openInventory(new ChooseInventoryHolder().getInventory());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if(!(e.getWhoClicked() instanceof Player)) return;
        Player player = (Player) e.getWhoClicked();

        if(e.getCurrentItem() == null)
            return;

        if(e.getCurrentItem().isSimilar(compass)) {
            e.setCancelled(true);
            return;
        }

        if(e.getClickedInventory() == null || e.getClickedInventory().getType() == InventoryType.CREATIVE)
            return;

        Inventory inv = e.getClickedInventory();
        if(!(inv.getHolder() instanceof ChooseInventoryHolder))
            return;

        e.setCancelled(true);
        if(e.getCurrentItem().getType() == Material.GRAY_STAINED_GLASS_PANE) return;
        String server = e.getCurrentItem().getItemMeta().getDisplayName().substring(2);
        LobbyPlugin.get().sendPluginMessage(player, "Connect", server);
        player.closeInventory();
    }

    @EventHandler
    public void onItemDrop(PlayerDropItemEvent e) {
        if(e.getItemDrop().getItemStack().isSimilar(compass)) e.setCancelled(true);
    }

    @EventHandler
    public void entityInteract(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if(!(entity instanceof LivingEntity && !(entity instanceof Player))) return;
        PersistentDataContainer nbt = entity.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(LobbyPlugin.get(), "serverNPC");
        String server = nbt.get(key, PersistentDataType.STRING);
        if(server == null) return;
        LobbyPlugin.get().sendPluginMessage(e.getPlayer(), "Connect", server);
    }

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(!(e.getEntity() instanceof Player)) return;
        e.setCancelled(true);
        if(e.getCause() != EntityDamageEvent.DamageCause.VOID) return;
        Player p = (Player) e.getEntity();
        Location spawn = (Location) LobbyPlugin.get().getConfig().get("worldspawn");
        if(spawn == null) spawn = p.getLocation().getWorld().getSpawnLocation();
        p.teleport(spawn);
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

}
