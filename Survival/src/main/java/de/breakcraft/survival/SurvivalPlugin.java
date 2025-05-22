package de.breakcraft.survival;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import de.breakcraft.survival.chunkclaims.ClaimManager;
import de.breakcraft.survival.commands.*;
import de.breakcraft.survival.listeners.*;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class SurvivalPlugin extends JavaPlugin implements PluginMessageListener {
    private static SurvivalPlugin instance;
    private Economy economy;
    private HikariDataSource dataSource;
    private ClaimManager ccm;
    private PlayerListener playerListener;

    @Override
    public void onEnable() {
        instance = this;

        var config = getConfig();
        if(config.get("mysql.ip") == null) {
            config.set("mysql.ip", "");
            config.set("mysql.port", 3306);
            config.set("mysql.database", "");
            config.set("mysql.user", "");
            config.set("mysql.password", "");
            saveConfig();
            getLogger().warning("[Breakcraft-Survival] Config Datei erstellt, deaktiviere Plugin !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if(!setupEconomy()) {
            getLogger().severe("[Breakcraft-Survival] Economy konnte nicht geladen werden !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getCommand("pawnshop").setExecutor(new Pawnshop());
        getCommand("craft").setExecutor(new Craft());
        getCommand("chunk").setExecutor(new Chunk());
        getCommand("chunk").setTabCompleter(new Chunk());
        getCommand("regeln").setExecutor(new Regeln());

        getServer().getMessenger().registerOutgoingPluginChannel(this, "breakcraft:proxy");
        getServer().getMessenger().registerIncomingPluginChannel(this, "breakcraft:proxy", this);

        PluginManager pm = Bukkit.getPluginManager();
        playerListener = new PlayerListener();
        pm.registerEvents(playerListener, this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new ChunkClaimListeners(), this);
        pm.registerEvents(new MessageListener(), this);

        var hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(
                String.format("jdbc:mysql://%s:%d/%s",
                        config.getString("mysql.ip"),
                        config.getInt("mysql.ip"),
                        config.getString("mysql.database"))
        );
        hikariConfig.setUsername(config.getString("mysql.user"));
        hikariConfig.setPassword(config.getString("mysql.password"));
        hikariConfig.setPoolName("breakcraft-survival");
        hikariConfig.setMaximumPoolSize(5);
        dataSource = new HikariDataSource(hikariConfig);

        ccm = new ClaimManager();
        ccm.initManager();
        playerListener.openBalanceListener();
    }

    @Override
    public void onDisable() {
        if(dataSource != null) dataSource.close();
        playerListener.closeListener();
    }

    public Economy getEconomy() {
        return economy;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public ClaimManager getClaimManager() {
        return ccm;
    }

    private boolean setupEconomy() {
        //dont need to check for Vault as plugin depends on it
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        return rsp != null && (economy = rsp.getProvider()) != null;
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
                            .findFirst().orElse("");
                    board.resetScores(old);
                    players.setScore(5);
                    p.setScoreboard(board);
                }

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static SurvivalPlugin getInstance() {
        return instance;
    }

}
