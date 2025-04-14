package de.breakcraft.lobby;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.breakcraft.lobby.Commands.ForcedSpawn;
import de.breakcraft.lobby.Commands.ServerNPC;
import de.breakcraft.lobby.Listener.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class Main extends JavaPlugin implements PluginMessageListener {
    public static Main instance;
    public static List<Integer> taskIDs = new ArrayList<>();
    private static ItemStack compass;

    @Override
    public void onEnable() {
        instance = this;

        setUpScoreboardScheduler();

        getCommand("forcedSpawn").setExecutor(new ForcedSpawn());
        getCommand("servernpc").setExecutor(new ServerNPC());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new JoinListener(), this);
        pm.registerEvents(new HealthFoodListener(), this);
        pm.registerEvents(new InteractListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new DoubleJumpListeners(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", this);

        compass = new ItemStack(Material.COMPASS);
        ItemMeta meta = compass.getItemMeta();
        meta.setDisplayName("§aServerauswahl");
        meta.addEnchant(Enchantment.DAMAGE_ALL, 1, true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        List<String> lore = Arrays.asList("", "§aDrücke Rechtsklick und sehe", "§aalle unsere Server / Gamemodis !");
        meta.setLore(lore);
        compass.setItemMeta(meta);

    }

    @Override
    public void onDisable() {
        for(Entity entity : ServerNPC.hitNPCs.keySet()) {
            ((LivingEntity) entity).setHealth(0);
        }
        ServerNPC.hitNPCs.clear();
    }

    public static Main getInstance() {
        return instance;
    }

    public static ItemStack getCompassItem() {
        return compass;
    }

    private void setUpScoreboardScheduler() {
        int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
            if(Bukkit.getOnlinePlayers().isEmpty()) return;

            Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);

            ByteArrayDataOutput out = ByteStreams.newDataOutput();

            out.writeUTF("PlayerCount");
            out.writeUTF("ALL");

            player.sendPluginMessage(this, "BungeeCord", out.toByteArray());

        }, 0, 40);
        taskIDs.add(i);
    }


    private HashMap<UUID, Integer> scoreboardPlayerCount = new HashMap<>();
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();

            if(!subchannel.equals("PlayerCount")) return;
            String server = in.readUTF();
            int playerCount = in.readInt();

            for(Player p : Bukkit.getOnlinePlayers()) {
                Scoreboard board = p.getScoreboard();
                Objective objective = (Objective) board.getObjectives().toArray()[0];
                if(!scoreboardPlayerCount.containsKey(p.getUniqueId()))
                    scoreboardPlayerCount.put(p.getUniqueId(), 0);
                Score players = objective.getScore("   §e" + playerCount + " §b/ §e1000");
                if(!players.isScoreSet()) {
                    board.resetScores("   §e" + scoreboardPlayerCount.get(p.getUniqueId()) + " §b/ §e1000");
                    players.setScore(5);
                    scoreboardPlayerCount.replace(p.getUniqueId(), playerCount);
                    p.setScoreboard(board);
                }

            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
