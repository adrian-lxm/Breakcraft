package de.breakcraft.lobby;

import de.breakcraft.lobby.commands.ServerNPC;
import de.breakcraft.lobby.commands.ServerSpawnPoint;
import de.breakcraft.lobby.listeners.*;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import java.io.*;

public class LobbyPlugin extends JavaPlugin implements PluginMessageListener {
    public static LobbyPlugin instance;

    @Override
    public void onEnable() {
        instance = this;

        getCommand("serverspawnpoint").setExecutor(new ServerSpawnPoint());
        ServerNPC serverNPC = new ServerNPC();
        PluginCommand command = getCommand("servernpc");
        command.setExecutor(serverNPC);
        command.setTabCompleter(serverNPC);

        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ServerListener(), this);
        pm.registerEvents(new PlayerListener(), this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, "breakcraft:proxy");
        getServer().getMessenger().registerIncomingPluginChannel(this, "breakcraft:proxy", this);

        getLogger().info("[Breakcraft-Lobby] Plugin initialized !");

    }

    public static LobbyPlugin get() {
        return instance;
    }


    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        try {
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
            String subchannel = in.readUTF();

            if(!subchannel.equals("PlayerCount")) return;
            in.readUTF(); //Server auslesen, allerdings ist Server nicht benötigt
            int playerCount = in.readInt();

            for(Player p : getServer().getOnlinePlayers()) {
                Scoreboard board = p.getScoreboard();
                Objective objective = (Objective) board.getObjectives().toArray()[0];
                Score players = objective.getScore("   §e" + playerCount + " §b/ §e100");
                if(!players.isScoreSet()) {
                    String old = board.getEntries().stream()
                            .filter(entry -> objective.getScore(entry).getScore() == 5)
                            .findFirst().orElse(null);
                    if(old == null) continue;
                    board.resetScores(old);
                    players.setScore(5);
                    p.setScoreboard(board);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPluginMessage(Player player, String sub, String... args) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(baos);
        try {
            out.writeUTF(sub);
            for(String s : args) out.writeUTF(s);
            player.sendPluginMessage(this, "breakcraft:proxy", baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
