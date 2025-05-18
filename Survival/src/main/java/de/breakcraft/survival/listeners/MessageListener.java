package de.breakcraft.survival.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        PermissionUser user = PermissionsEx.getUser(e.getPlayer());
        e.setFormat((user.getPrefix() + " " + e.getPlayer().getName() + " §f: ").replace('&', '§') + e.getMessage());
    }

}
