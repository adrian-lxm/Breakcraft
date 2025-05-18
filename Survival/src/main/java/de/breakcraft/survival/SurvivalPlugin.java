package de.breakcraft.survival;

import com.mysql.cj.jdbc.MysqlDataSource;
import de.breakcraft.survival.commands.*;
import de.breakcraft.survival.economy.EconomyImpl;
import de.breakcraft.survival.listeners.*;
import de.breakcraft.survival.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SurvivalPlugin extends JavaPlugin {
    private EconomyImpl economy;
    public static DataSource dataSource;
    public static ChunkClaimManager ccm;
    private static SurvivalPlugin instance;
    public static HashMap<Player, Score[]> infos = new HashMap<Player, Score[]>();
    public List<Integer> taskIDs = new ArrayList<>();
    public static int playerCount = 0;

    @Override
    public void onEnable() {
        instance = this;


        MysqlDataSource mysqlDataSource = new MysqlDataSource();
        mysqlDataSource.setUrl("jdbc:mysql://localhost:3306/Breakcraft?characterEncoding=utf8&autoReconnect=true");
        mysqlDataSource.setUser("client_minecraft");
        mysqlDataSource.setPassword("nAuE@&3d]!(h");
        dataSource = mysqlDataSource;

        getCommand("pawnshop").setExecutor(new Pawnshop());
        getCommand("craft").setExecutor(new Craft());
        getCommand("chunk").setExecutor(new Chunk());
        getCommand("chunk").setTabCompleter(new Chunk());
        getCommand("regeln").setExecutor(new Regeln());

        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new PlayerJoinListener(), this);
        pm.registerEvents(new InventoryListener(), this);
        pm.registerEvents(new ChunkClaimListeners(), this);
        pm.registerEvents(new MessageListener(), this);

        ccm = new ChunkClaimManager();
        ccm.initManager();

        setUpScreensScheduler();
    }

    @Override
    public void onDisable() {
        for(int i : taskIDs) {
            Bukkit.getScheduler().cancelTask(i);
        }
    }

    public static SurvivalPlugin getInstance() {
        return instance;
    }

    private void setUpScreensScheduler() {
        int i = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                if(!Bukkit.getOnlinePlayers().isEmpty()) {
                    for(Player p : Bukkit.getOnlinePlayers()) {
                        Scoreboard board = p.getScoreboard();
                        Objective objective = (Objective) board.getObjectives().toArray()[0];
                        double money = economy.getBalance(p);
                        Score balance = objective.getScore("   §e" + money + " €");
                        boolean isUpdated = false;
                        if (!balance.isScoreSet()) {
                            board.resetScores(infos.get(p)[1].getEntry());
                            balance.setScore(2);
                            Score[] replace = {infos.get(p)[0], balance};
                            infos.replace(p, replace);
                            isUpdated = true;
                        }

                        Score players = objective.getScore("   §e" + playerCount + " §b/ §e1000");
                        if (!players.isScoreSet()) {
                            board.resetScores(infos.get(p)[0].getEntry());
                            players.setScore(8);
                            Score[] replace = {players, infos.get(p)[1]};
                            infos.replace(p, replace);
                            isUpdated = true;
                        }

                        if (isUpdated) p.setScoreboard(board);

                    }
                }
            }
        }, 0, 20);
        taskIDs.add(i);
    }

    public static EconomyImpl getEconomy() {
        return instance.economy;
    }

}
