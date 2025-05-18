package de.breakcraft.lobby.Listener;

import de.breakcraft.lobby.LobbyPlugin;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class HealthFoodListener implements Listener {

    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
            if(e.getCause() == EntityDamageEvent.DamageCause.VOID) {
                Player p = (Player) e.getEntity();
                if(LobbyPlugin.getInstance().getConfig().get("forced-spawn") != null) {
                    Location forcedSpawn = (Location) LobbyPlugin.getInstance().getConfig().get("forced-spawn");
                    p.teleport(forcedSpawn);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent e) {
        if(e.getEntity() instanceof Player) {
            e.setCancelled(true);
        }
    }

}
