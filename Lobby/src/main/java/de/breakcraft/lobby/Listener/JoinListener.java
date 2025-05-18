package de.breakcraft.lobby.Listener;


import de.breakcraft.lobby.LobbyPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;

import java.util.Optional;

public class JoinListener implements Listener {
    private Scoreboard prefixManager;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        for(String s : p.getListeningPluginChannels())
            System.out.println(s + " - ");

        if(prefixManager == null) prefixManager = Bukkit.getScoreboardManager().getNewScoreboard();

        Object spawn = LobbyPlugin.getInstance().getConfig().get("forced-spawn");
        if(spawn != null) {
            Location forcedSpawn = (Location) spawn;
            p.teleport(forcedSpawn);
        }
        p.getInventory().setItem(0, LobbyPlugin.getCompassItem());

        LuckPerms luckPerms = LuckPermsProvider.get();
        luckPerms.getUserManager().loadUser(p.getUniqueId()).thenAcceptAsync((user -> {
            String prefix;
            System.out.println(user.getPrimaryGroup());
            Optional<Group> group = luckPerms.getGroupManager().loadGroup(user.getPrimaryGroup()).join();
            prefix = group.map(value -> value.getDisplayName().replace('&', '§')).orElse("");

            Bukkit.getScheduler().runTask(LobbyPlugin.getInstance(), () -> {

                if(prefixManager.getTeam(user.getPrimaryGroup()) == null) {
                    prefixManager.registerNewTeam(user.getPrimaryGroup()).setPrefix(" " + prefix + " ");
                }
                prefixManager.getTeam(user.getPrimaryGroup()).addEntry(p.getName());
                p.setPlayerListName(" " + prefix + " " + p.getName());
                p.setDisplayName(" " + prefix + " " + p.getName());

                ScoreboardManager sbm = Bukkit.getScoreboardManager();
                Scoreboard board = sbm.getNewScoreboard();
                Objective objective = board.registerNewObjective("dummy", "");
                objective.setDisplayName(ChatColor.DARK_PURPLE + "Breakcraft.de");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                Score blank = objective.getScore("  ");
                blank.setScore(1);
                Score rank = objective.getScore("   " + prefix);
                rank.setScore(2);
                Score rankDesc = objective.getScore("§bRank:");
                rankDesc.setScore(3);
                Score blank3 = objective.getScore(" ");
                blank3.setScore(4);
                Score players = objective.getScore("   §e0 §b/ §e100");
                players.setScore(5);
                Score playersDesc = objective.getScore("§bSpieler:");
                playersDesc.setScore(6);
                Score blank4 = objective.getScore("    ");
                blank4.setScore(7);
                Score Gamemode = objective.getScore("   §eLobby");
                Gamemode.setScore(8);
                Score GamemodeDesc = objective.getScore("§bGamemode:");
                GamemodeDesc.setScore(9);
                Score blank5 = objective.getScore("     ");
                blank5.setScore(10);

                p.setScoreboard(board);
            });

        }));

        e.setJoinMessage(null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage(null);
        e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(e.getPlayer().getUniqueId());
        Team team = prefixManager.getTeam(user.getPrimaryGroup());
        if(team != null) team.removeEntry(e.getPlayer().getName());
    }

}
