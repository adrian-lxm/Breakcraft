package de.breakcraft.proxy.db;

import com.velocitypowered.api.proxy.Player;
import de.breakcraft.proxy.ProxyPlugin;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseManager {
    private static DatabaseManager instance;
    private final DataSource dataSource;
    private final Map<UUID, BanEntry> activeBans;

    private DatabaseManager(DataSource source) {
        this.dataSource = source;
        activeBans = new ConcurrentHashMap<>();
    }

    public static void initialize(DataSource source) {
        instance = new DatabaseManager(source);
        try(Connection con = instance.dataSource.getConnection();
            PreparedStatement prep = con.prepareStatement("SELECT * FROM bans WHERE duration = -1 OR ? < (duration + timestamp)")) {
            prep.setLong(1, System.currentTimeMillis());
            ResultSet set = prep.executeQuery();
            while (set.next()) {
                BanEntry entry = new BanEntry(
                        set.getInt(1),
                        UUID.fromString(set.getString(2)),
                        set.getLong(3),
                        set.getLong(4),
                        set.getString(5));
                instance.activeBans.put(entry.getUuid(), entry);
            }
            set.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ProxyPlugin.get().getLogger().info("Es wurden " + instance.activeBans.size() + " aktive Bans aus der Datenbank geladen !");
    }

    public static DatabaseManager get() {
        return instance;
    }

    public CompletableFuture<BanEntry> addBan(Player player, long duration, String reason) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection con = dataSource.getConnection();
                PreparedStatement prep = con.prepareStatement("INSERT INTO bans (uuid, timestamp, duration, reason) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                prep.setString(1, player.getUniqueId().toString());
                long millis = System.currentTimeMillis();
                prep.setLong(2, millis);
                prep.setLong(3, duration);
                if(reason == null)
                    prep.setNull(4, Types.VARCHAR);
                else
                    prep.setString(4, reason);
                prep.execute();
                ResultSet set = prep.getGeneratedKeys();
                set.next();
                BanEntry entry = new BanEntry(set.getInt(1), player.getUniqueId(), millis, duration, reason);
                set.close();
                activeBans.put(entry.getUuid(), entry);
                return entry;
            } catch (SQLException e) {
                ProxyPlugin.get().getLogger()
                        .severe("Error while creating ban entry: " + e.getMessage() + " !");
                return null;
            }
        });
    }

    public CompletableFuture<Boolean> removeBan(BanEntry entry) {
        return CompletableFuture.supplyAsync(() -> {
            if(entry.isActive()) return false;
            try(Connection con = dataSource.getConnection();
                PreparedStatement prep = con.prepareStatement("UPDATE bans SET duration = 0 WHERE id = ?")) {
                prep.setInt(1, entry.getId());
                if(prep.executeUpdate() == 1) {
                    activeBans.remove(entry.getUuid());
                    return true;
                }
                return false;
            } catch (SQLException e) {
                ProxyPlugin.get().getLogger().severe("Error while removing ban: " + e.getMessage() + " !");
                return false;
            }
        });
    }

    public CompletableFuture<BanEntry> getLastEntry(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            BanEntry entry = activeBans.get(player);
            if(entry != null) return entry;
            try(Connection con = dataSource.getConnection();
                PreparedStatement prep = con.prepareStatement("SELECT * FROM bans WHERE uuid = ? ORDER BY timestamp DESC LIMIT 1")) {
                prep.setString(1, player.toString());
                ResultSet set = prep.executeQuery();
                while (set.next()) {
                    entry = new BanEntry(
                            set.getInt(1),
                            player,
                            set.getLong(3),
                            set.getLong(4),
                            set.getString(5)
                    );
                }
                set.close();
                return entry;
            } catch (SQLException e) {
                ProxyPlugin.get().getLogger().severe("Error while fetching ban entry: " + e.getMessage() + " !");
                return null;
            }
        });
    }

    public CompletableFuture<BanEntry[]> getAllEntries(UUID player) {
        return CompletableFuture.supplyAsync(() -> {
            try(Connection con = dataSource.getConnection();
                PreparedStatement prep = con.prepareStatement("SELECT * FROM bans WHERE uuid = ? ORDER BY timestamp DESC")) {
                prep.setString(1, player.toString());
                ResultSet set = prep.executeQuery();
                List<BanEntry> entries = new ArrayList<>();
                while (set.next()) {
                    entries.add(new BanEntry(
                            set.getInt(1),
                            player,
                            set.getLong(3),
                            set.getLong(4),
                            set.getString(5)
                    ));
                }
                set.close();
                return (BanEntry[]) entries.toArray();
            } catch (SQLException e) {
                ProxyPlugin.get().getLogger().severe("Error while fetching ban entry: " + e.getMessage() + " !");
                return null;
            }
        });
    }

    public Map<UUID, BanEntry> getActiveBans() {
        return activeBans;
    }

}
