package de.breakcraft.survival.listeners;

import de.breakcraft.survival.SurvivalPlugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scoreboard.*;
import java.util.*;

public class PlayerListener implements Listener {
    private final HashMap<UUID, Double> balances = new HashMap<>();
    private WorldBorder safezone;

    @EventHandler
    public void onWorldLoaded(WorldLoadEvent event) {
        var world = event.getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        int size = SurvivalPlugin.get().getConfig().getInt("safezone");
        safezone = Bukkit.createWorldBorder();
        safezone.setSize(size);
        safezone.setCenter(world.getSpawnLocation());
        safezone.setDamageBuffer(10);
        safezone.setDamageAmount(0);
        safezone.setWarningDistance(0);
    }

    public void openBalanceListener() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(SurvivalPlugin.get(), () -> {
            var economy = SurvivalPlugin.get().getEconomy();
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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        LuckPerms luckPerms = LuckPermsProvider.get();
        luckPerms.getUserManager().loadUser(p.getUniqueId()).thenAccept((user -> {
            String prefix;
            Optional<Group> group = luckPerms.getGroupManager().loadGroup(user.getPrimaryGroup()).join();
            prefix = group.map(value -> value.getDisplayName().replace('&', '§')).orElse("");

            Bukkit.getScheduler().runTask(SurvivalPlugin.get(), () -> {

                p.setPlayerListName(prefix + " " + p.getName());
                p.setDisplayName(prefix + " " + p.getName());
                Scoreboard mainBoard = Bukkit.getScoreboardManager().getMainScoreboard();
                Team prefixTeam = mainBoard.getTeam(user.getPrimaryGroup());
                if(prefixTeam == null) {
                    prefixTeam = mainBoard.registerNewTeam(user.getPrimaryGroup());
                    prefixTeam.setPrefix(String.format("%s ", prefix));
                }
                prefixTeam.addEntry(p.getName());

                Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
                Objective objective = board.registerNewObjective("dummy", "");
                objective.setDisplayName(ChatColor.DARK_PURPLE + "Breakcraft.de");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                Score blank = objective.getScore(" ");
                blank.setScore(1);
                double money = SurvivalPlugin.get().getEconomy().getBalance(p);
                balances.put(p.getUniqueId(), money);
                Score balance = objective.getScore("§e" + money + " €");
                balance.setScore(2);
                Score balanceDesc = objective.getScore("§bKontostand:");
                balanceDesc.setScore(3);
                Score blank2 = objective.getScore("  ");
                blank2.setScore(4);
                Score rank = objective.getScore(prefix);
                rank.setScore(5);
                Score rankDesc = objective.getScore("§bRank:");
                rankDesc.setScore(6);
                Score blank3 = objective.getScore("   ");
                blank3.setScore(7);
                Score players = objective.getScore("§e0 §b/ §e100");
                players.setScore(8);
                Score playersDesc = objective.getScore("§bSpieler:");
                playersDesc.setScore(9);
                Score blank4 = objective.getScore("    ");
                blank4.setScore(10);
                Score Gamemode = objective.getScore("§eSurvival");
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

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if(!(event.getEntity() instanceof LivingEntity)) return;
        if(event.getLocation().getWorld().getEnvironment() != World.Environment.NORMAL) return;
        if(safezone.isInside(event.getLocation())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        e.setQuitMessage("§e[§c-§e] " + e.getPlayer().getName());
        e.getPlayer().setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
        LuckPerms luckPerms = LuckPermsProvider.get();
        User user = luckPerms.getUserManager().getUser(e.getPlayer().getUniqueId());
        Team team = Bukkit.getScoreboardManager().getMainScoreboard().getTeam(user.getPrimaryGroup());
        if(team != null) team.removeEntry(e.getPlayer().getName());
        balances.remove(e.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        e.setFormat("%s§f: %s");
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        var world = p.getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        if(e.getAction() != Action.PHYSICAL && e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        double distance = p.getLocation().distance(world.getSpawnLocation());
        if(distance >= safezone.getSize()) return;
        if(!p.hasPermission("breakcraft.safespawn")) {
            e.setCancelled(true);
            p.sendMessage("§cDu bist noch im sicheren Spawnbereich! Entferne dich vom Spawn um §e"  + (int) (250 - distance) + " §cBlöcken!");
        }
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        if(safezone.isInside(event.getRespawnLocation()))
            event.getPlayer().setWorldBorder(safezone);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        var p = event.getPlayer();
        var from = event.getFrom();
        var dir = from.getDirection();

        if(safezone.isInside(from) && !safezone.isInside(from.add(dir.multiply(1.5)))) {
                p.setWorldBorder(null);
        } else if(safezone.isInside(from) && safezone.isInside(from.add(dir.multiply(-1.5))))
            p.setWorldBorder(safezone);
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!(e.getEntity() instanceof Player p)) return;
        var world = p.getWorld();
        if(world.getEnvironment() != World.Environment.NORMAL) return;
        if(!safezone.isInside(p.getLocation())) return;
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
        if(!safezone.isInside(e.getLocation())) return;
        e.setCancelled(true);
    }

}
