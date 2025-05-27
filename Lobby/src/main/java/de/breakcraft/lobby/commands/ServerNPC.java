package de.breakcraft.lobby.commands;

import de.breakcraft.lobby.LobbyPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

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
                PersistentDataContainer nbt = entity.getPersistentDataContainer();
                String server = nbt.get(key, PersistentDataType.STRING);
                if(server == null) continue;
                if (args[1].equals("all") || server.equals(args[1])) {
                    entity.remove();
                }

            }

            return true;
        }

        try {
            EntityType type = EntityType.valueOf(args[1]);
            LivingEntity entity = (LivingEntity) p.getWorld().spawnEntity(p.getLocation(), type);
            entity.setRemoveWhenFarAway(false);
            entity.setAI(false);
            entity.setCustomName("§a" + args[0] + " §f[§eRechtsklick§f]");
            entity.setCustomNameVisible(true);
            PersistentDataContainer nbt = entity.getPersistentDataContainer();
            nbt.set(key, PersistentDataType.STRING, args[0]);
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

                        PersistentDataContainer nbt = entity.getPersistentDataContainer();
                        NamespacedKey key = new NamespacedKey(LobbyPlugin.get(), "serverNPC");
                        String server = nbt.get(key, PersistentDataType.STRING);
                        if(server == null) continue;
                        list.add(server);
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
