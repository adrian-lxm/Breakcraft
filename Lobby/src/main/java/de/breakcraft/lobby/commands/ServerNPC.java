package de.breakcraft.lobby.commands;

import de.breakcraft.lobby.LobbyPlugin;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ServerNPC implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if (!p.hasPermission("breakcraft.spawnNPC")) {
            p.sendMessage("§cDu hast nicht die Rechte dazu !");
            return false;
        }

        if (args.length != 2) {
            p.sendMessage("§cGebe den Server und Entity-Typ an !");
            return false;
        }

        NamespacedKey key = new NamespacedKey(LobbyPlugin.get(), "serverNPC");

        if (args[0].equals("clear")) {
            List<Entity> entities = p.getWorld().getEntities();

            for (Entity entity : entities) {
                if (!(entity instanceof LivingEntity && !(entity instanceof Player))) continue;
                net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
                NBTTagCompound tag = new NBTTagCompound();
                nmsEntity.c(tag);
                if(!tag.hasKey("serverNPC")) continue;
                String server = tag.getString("serverNPC");
                if (args[1].equals("all") || server.equals(args[1])) {
                    entity.remove();
                }

            }

            return true;
        }

        try {
            EntityType type = EntityType.valueOf(args[1]);
            LivingEntity entity = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), type);
            if(entity.getEquipment() != null) entity.getEquipment().clear();
            entity.setRemoveWhenFarAway(false);
            entity.setInvulnerable(true);
            entity.setSilent(true);
            entity.setAI(false);
            entity.setCustomName("§a" + args[0] + " §f[§eRechtsklick§f]");
            entity.setCustomNameVisible(true);
            net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
            NBTTagCompound nbt = new NBTTagCompound();
            nmsEntity.c(nbt);
            nbt.setString("serverNPC", args[0].toLowerCase());
            nmsEntity.c(nbt);
            p.sendMessage("§aEntity erstellt für Server §e" + args[0] + " !");
        } catch (IllegalArgumentException ignored) {

        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String s, String[] args) {
        List<String> list = new ArrayList<>();
        if(!(sender instanceof Player)) return list;
        Player p = (Player) sender;

        switch (args.length) {
            case 1:
                list.add("clear");
                break;

            case 2:
                if(args[0].equalsIgnoreCase("clear")) {
                    for(Entity entity : p.getWorld().getEntities()) {
                        if(!(entity instanceof LivingEntity && !(entity instanceof Player))) continue;

                        net.minecraft.server.v1_13_R2.Entity nmsEntity = ((CraftEntity) entity).getHandle();
                        NBTTagCompound tag = new NBTTagCompound();
                        nmsEntity.c(tag);
                        if(!tag.hasKey("serverNPC")) continue;
                        list.add(tag.getString("serverNPC"));
                    }
                    list.add("all");
                    break;
                }

                for(EntityType type : EntityType.values()) {
                    if(!type.isAlive()) continue;
                    if(type.toString().toLowerCase().startsWith(args[1]))
                        list.add(type.toString());
                }

                break;

            default:
                break;
        }

        return list;
    }

}
