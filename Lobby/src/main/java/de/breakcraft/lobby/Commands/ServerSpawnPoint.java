package de.breakcraft.lobby.commands;

import de.breakcraft.lobby.LobbyPlugin;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ServerSpawnPoint implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player p = (Player) sender;
            if(p.hasPermission("breakcraft.serverspawnpoint")) {
                LobbyPlugin.get().getConfig().set("worldspawn", p.getLocation());
                LobbyPlugin.get().saveConfig();
                p.sendMessage("[§aBreackcraft§f] §aServer Spawnpoint wurde gesetzt !");
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
            } else p.sendMessage("[§aBreackcraft§f] §cDazu hast du nicht die Rechte !");
        }
        return false;
    }

}
