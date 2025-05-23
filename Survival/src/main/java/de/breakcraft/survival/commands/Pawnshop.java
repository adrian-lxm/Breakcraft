package de.breakcraft.survival.commands;

import de.breakcraft.survival.pawnshop.PawnshopHolder;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Pawnshop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player p)) return false;
        PawnshopHolder pawnshopHolder = new PawnshopHolder();
        p.openInventory(pawnshopHolder.getInventory());
        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
    }

}
