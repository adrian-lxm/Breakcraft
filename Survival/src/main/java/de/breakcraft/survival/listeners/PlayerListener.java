package de.breakcraft.survival.listeners;

import de.breakcraft.survival.SurvivalPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.*;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

public class PlayerListener implements Listener {
    private Scoreboard prefixManager;
    private final HashMap<UUID, Double> balances = new HashMap<>();

    public void openBalanceListener() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SurvivalPlugin.getInstance(), () -> {
            var economy = SurvivalPlugin.getInstance().getEconomy();
            for(Player player : Bukkit.getOnlinePlayers()) {
                if(!balances.containsKey(player.getUniqueId())) continue;
                double balance = economy.getBalance(player);
                if(balances.get(player.getUniqueId()) == balance) continue;
                Scoreboard board = player.getScoreboard();
                Objective objective = (Objective) board.getObjectives().toArray()[0];
                Score newBalance = objective.getScore("   §e" + balance + " €");
                String oldBalance = board.getEntries().stream()
                        .filter(entry -> objective.getScore(entry).getScore() == 2)
                        .findFirst().orElse("");
                board.resetScores(oldBalance);
                newBalance.setScore(2);
                player.setScoreboard(board);
            }
        }, 0, (long) (20 * 2.5));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if(prefixManager == null)
            prefixManager = Bukkit.getScoreboardManager().getNewScoreboard();

        LuckPerms luckPerms = LuckPermsProvider.get();
        luckPerms.getUserManager().loadUser(p.getUniqueId()).thenAccept((user -> {
            String prefix;
            Optional<Group> group = luckPerms.getGroupManager().loadGroup(user.getPrimaryGroup()).join();
            prefix = group.map(value -> value.getDisplayName().replace('&', '§')).orElse("");

            Bukkit.getScheduler().runTask(SurvivalPlugin.getInstance(), () -> {

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
                Score blank = objective.getScore(" ");
                blank.setScore(1);
                double money = SurvivalPlugin.getInstance().getEconomy().getBalance(p);
                balances.put(p.getUniqueId(), money);
                Score balance = objective.getScore("   §e" + money + " €");
                balance.setScore(2);
                Score balanceDesc = objective.getScore("§bKontostand:");
                balanceDesc.setScore(3);
                Score blank2 = objective.getScore("  ");
                blank2.setScore(4);
                Score rank = objective.getScore("   " + prefix);
                rank.setScore(5);
                Score rankDesc = objective.getScore("§bRank:");
                rankDesc.setScore(6);
                Score blank3 = objective.getScore("   ");
                blank3.setScore(7);
                Score players = objective.getScore("   §e0 §b/ §e100");
                players.setScore(8);
                Score playersDesc = objective.getScore("§bSpieler:");
                playersDesc.setScore(9);
                Score blank4 = objective.getScore("    ");
                blank4.setScore(10);
                Score Gamemode = objective.getScore("   §eSurvival");
                Gamemode.setScore(11);
                Score GamemodeDesc = objective.getScore("§bGamemode:");
                GamemodeDesc.setScore(12);
                Score blank5 = objective.getScore("     ");
                blank5.setScore(13);

                p.setScoreboard(board);
            });

        }));
        e.setJoinMessage("§e[§a+§e] " + p.getName());

    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage("§e[§c-§e] " + e.getPlayer().getName());
        e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(e.getPlayer().getUniqueId());
        Team team = prefixManager.getTeam(user.getPrimaryGroup());
        if(team != null) team.removeEntry(e.getPlayer().getName());
        balances.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setFormat("%s: %s");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        var world = p.getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        if(e.getAction() != Action.PHYSICAL && e.getAction() != Action.LEFT_CLICK_BLOCK) return;
        double distance = p.getLocation().distance(world.getSpawnLocation());
        if(distance >= 250) return;
        if(!p.hasPermission("breakcraft.safespawn")) {
            e.setCancelled(true);
            p.sendMessage("§cDu bist noch im sicheren Spawnbereich! Entferne dich vom Spawn um §e"  + (int) (250 - distance) + " §cBlöcken!");
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof Player p)) return;
        var world = p.getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        double distance = p.getLocation().distance(world.getSpawnLocation());
        if(distance >= 250) return;
        if(!p.hasPermission("breakcraft.safespawn")) {
            e.setCancelled(true);
            if(e.getDamager() instanceof Player p2)
                p2.sendMessage("§cDieser Spieler ist noch im sicheren Spawnbereich !");
        }
    }

    @EventHandler
    public void onBlockBreak(EntityExplodeEvent e) {
        var world = e.getEntity().getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        double distance = e.getLocation().distance(world.getSpawnLocation());
        if(distance >= 250) return;
        e.setCancelled(true);
    }

}
