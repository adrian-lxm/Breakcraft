package de.breakcraft.survival.chunkclaims;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.OfflinePlayer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChunkClaim {
    private int id;
    private UUID owner;

    private String world;

    private int chunkX;

    private int chunkZ;

    private volatile UUID[] trustedPlayers;

    public ChunkClaim(Chunk claim, UUID owner) {
        this.owner = owner;
        this.world = claim.getWorld().getName();
        this.chunkX = claim.getX();
        this.chunkZ = claim.getZ();
        trustedPlayers = new UUID[0];
    }

    public ChunkClaim(int id, UUID owner, String world, int chunkX, int chunkZ, UUID[] trustedPlayers) {
        this.id = id;
        this.owner = owner;
        this.world = world;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
        this.trustedPlayers = trustedPlayers;
    }

    public CompletableFuture<Boolean> saveToDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try(var con = SurvivalPlugin.getInstance().getDataSource().getConnection();
                var statement = con.prepareStatement("INSERT INTO chunkclaim (uuid, world, x, z, flags) values (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, owner.toString());
                statement.setString(2, world);
                statement.setInt(3, chunkX);
                statement.setInt(4, chunkZ);
                statement.setNull(5, Types.VARCHAR);
                statement.executeUpdate();

                try(ResultSet set = statement.getGeneratedKeys()) {
                    if(!set.next()) return false;
                    id = set.getInt(1);
                }
            } catch (SQLException e) {
                return false;
            }
            return true;
        });
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isOwner(UUID uuid) {
        return owner.equals(uuid);
    }

    public boolean isTrusted(UUID id) {
        for(var uuid : trustedPlayers) {
            if(id.equals(uuid)) return true;
        }
        return false;
    }

    public CompletableFuture<Boolean> addTrusted(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try(var con = SurvivalPlugin.getInstance().getDataSource().getConnection();
                var statement = con.prepareStatement("INSERT INTO trustedplayer values (?, ?)")) {
                statement.setInt(1, this.id);
                statement.setString(2, id.toString());
                if(statement.executeUpdate() != 1) return false;
            } catch (SQLException e) {
                return false;
            }
            var newArray = Arrays.copyOf(trustedPlayers, trustedPlayers.length + 1);
            newArray[newArray.length - 1] = id;
            trustedPlayers = newArray;
            return true;
        });
    }

    public CompletableFuture<Boolean> removeTrusted(UUID id) {
        return CompletableFuture.supplyAsync(() -> {
            try(var con = SurvivalPlugin.getInstance().getDataSource().getConnection();
                var statement = con.prepareStatement("DELETE FROM trustedplayer WHERE claimID = ? AND uuid = ?")) {
                statement.setInt(1, this.id);
                statement.setString(2, id.toString());
                if(statement.executeUpdate() != 1) return false;
            } catch (SQLException e) {
                return false;
            }
            var newArray = Arrays.copyOf(trustedPlayers, trustedPlayers.length - 1);
            for(int i = 0, j = 0; i < newArray.length+1; i++) {
                if(trustedPlayers[i].equals(id)) continue;
                newArray[j] = trustedPlayers[i];
                j++;
            }
            trustedPlayers = newArray;
            return true;
        });
    }

    public int getId() {
        return id;
    }

    public String getWorld() {
        return world;
    }

    public int getChunkX() {
        return chunkX;
    }

    public int getChunkZ() {
        return chunkZ;
    }

    public UUID[] getTrustedPlayers() {
        return trustedPlayers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, owner, world, chunkX, chunkZ, Arrays.hashCode(trustedPlayers));
    }
}
