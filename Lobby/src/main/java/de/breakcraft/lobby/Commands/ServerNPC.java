package de.breakcraft.lobby.Commands;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

public class ServerNPC implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if(!p.hasPermission("breakcraft.spawnNPC")) {
            p.sendMessage("§cDu hast nicht die Rechte dazu !");
            return false;
        }

        if(args.length != 2) {
            p.sendMessage("§cGebe den Server an !");
            return false;
        }

        if (args[0].equals("clear")) {
            List<Entity> entities = p.getWorld().getEntities();

            for(Entity entity : entities) {
                if(!(entity instanceof LivingEntity && !(entity instanceof Player))) continue;

                net.minecraft.server.v1_16_R3.Entity craftEntity = ((CraftEntity) entity).getHandle();
                NBTTagCompound nbt = new NBTTagCompound();
                craftEntity.save(nbt);

                if(!nbt.hasKey("serverNPC")) continue;

                if(args[1].equalsIgnoreCase("all") || nbt.getString("serverNPC").equals(args[1])) {
                    entity.remove();
                }

            }

            return true;
        }

        EntityType type = EntityType.valueOf(args[1].toUpperCase());
        Entity entity = p.getWorld().spawnEntity(p.getLocation(), EntityType.ZOMBIE);
        setNBTData(entity, args[0]);
        entity.setCustomName("§a" + args[0] + " §f[§eRechtsklick§f]");
        entity.setCustomNameVisible(true);
        p.sendMessage("§aEntity erstellt für Server §e" + args[0] + " !");

        return true;
    }

    private void setNBTData(Entity bukkitEntity, String server) {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        nmsEntity.save(nbt);
        nbt.setBoolean("NoAI", true);
        nbt.setBoolean("Invulnerable", true);
        nbt.setBoolean("PersistenceRequired", true);
        nbt.setString("serverNPC", server);
        nmsEntity.load(nbt);
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

                        net.minecraft.server.v1_16_R3.Entity craftEntity = ((CraftEntity) entity).getHandle();
                        NBTTagCompound nbt = new NBTTagCompound();
                        craftEntity.save(nbt);

                        if(!nbt.hasKey("serverNPC")) continue;

                        list.add(nbt.getString("serverNPC"));
                    }
                    list.add("all");
                    break;
                }

                for(EntityType type : EntityType.values()) {
                    if(!type.isAlive()) continue;
                    if(type.toString().toLowerCase().startsWith(args[1]))
                        list.add(type.name());
                }

                break;

            default:
                break;
        }

        return list;
    }

}
