package de.breakcraft.survival.listeners;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class MessageListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        LuckPerms luckPerms = LuckPermsProvider.get();
        var user = luckPerms.getUserManager().getUser(e.getPlayer().getUniqueId());
        var group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        String message = group.getDisplayName().replace('&', '§') + " %s §f: %s";
        e.setFormat(message);
    }

}
