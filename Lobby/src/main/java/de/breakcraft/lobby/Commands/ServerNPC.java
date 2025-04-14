package de.breakcraft.lobby.Commands;

import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;

public class ServerNPC implements CommandExecutor {
    public static HashMap<Entity, String> hitNPCs = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if(!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        if(!p.hasPermission("breakcraft.spawnNPC")) {
            p.sendMessage("§cDu hast nicht die Rechte dazu !");
            return false;
        }

        if(args.length == 2) {
            switch(args[0]) {
                case "Survival":
                    EntityType type = EntityType.valueOf(args[1]);
                    Entity entity = p.getWorld().spawnEntity(p.getLocation(), EntityType.ZOMBIE);
                    noAI(entity);
                    entity.setCustomName("§aSurvival §f[§eRechtsklick§f]");
                    entity.setCustomNameVisible(true);
                    hitNPCs.put(entity, "Survival");
                    p.sendMessage("§aEntity erstellt !");
                    break;

                case "clear":
                    for(Entity entities : hitNPCs.keySet()) {
                        ((LivingEntity) entities).setHealth(0);
                    }
                    hitNPCs.clear();
                    break;
                default:
                    break;
            }
        } else p.sendMessage("§cGebe den Server an !");
        return false;
    }

    private void noAI(Entity bukkitEntity) {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound nbt = new NBTTagCompound();
        nmsEntity.c(nbt);
        nbt.setBoolean("NoAI", true);
        nmsEntity.f(nbt);
    }
}
