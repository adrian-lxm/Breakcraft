package de.breakcraft.lobby.Listener;

import de.breakcraft.lobby.LobbyPlugin;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.Arrays;
import java.util.List;

public class InteractListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(!(e.getPlayer().hasPermission("breakcraft.lobby.interact"))) e.setCancelled(true);

        System.out.println(e.getAction().name());

        if(!(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK))
            return;


        if(e.getItem() == null)
            return;

        if(!e.getItem().isSimilar(LobbyPlugin.getCompassItem()))
            return;

        Inventory inv = Bukkit.createInventory(null, 3*9, "§5Serverauswahl : Breakcraft");
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
                "§aAktuelle Minecraft-Version: §e1.17.1",
                "§cBitte spiele, wenn möglich, mit mind. 1.13 !"
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
                "§aSpiele Minecraft mit deutlichen Verschwerungen durch !",
                ChatColor.RED + "Derzeit nicht verfügbar !"
        );
        meta.setLore(lore);
        sapling.setItemMeta(meta);

        for(int i = 0; i < 9; i++) {
            inv.setItem(i, glass);
        }

        inv.setItem(9, glass);
        inv.setItem(17, glass);

        inv.setItem(10, grass);
        inv.setItem(11, sapling);

        for(int i = 18; i < inv.getSize(); i++) {
            inv.setItem(i, glass);
        }

        e.getPlayer().openInventory(inv);
    }

    @EventHandler
    public void entityInteract(PlayerInteractAtEntityEvent e) {
        Entity entity = e.getRightClicked();
        if(!(entity instanceof LivingEntity && !(entity instanceof Player))) return;

        net.minecraft.server.v1_16_R3.Entity craftEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        craftEntity.save(nbt);

        if(!nbt.hasKey("serverNPC")) return;

        LobbyPlugin.getInstance()
                .sendPluginMessage(e.getPlayer(), "Connect", nbt.getString("serverNPC"));

    }

}
