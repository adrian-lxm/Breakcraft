package de.breakcraft.survival.chunkclaims;

import de.breakcraft.survival.SurvivalPlugin;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ChunkClaim {
    private int id;
    private final UUID owner;

    private final ChunkKey chunkKey;

    private volatile UUID[] trustedPlayers;

    public ChunkClaim(ChunkKey key, UUID owner) {
        this.owner = owner;
        chunkKey = key;
        trustedPlayers = new UUID[0];
    }

    public ChunkClaim(int id, UUID owner, ChunkKey key, UUID[] trustedPlayers) {
        this.id = id;
        this.owner = owner;
        chunkKey = key;
        this.trustedPlayers = trustedPlayers;
    }

    public CompletableFuture<ChunkClaim> saveToDatabase() {
        return CompletableFuture.supplyAsync(() -> {
            try(var con = SurvivalPlugin.getInstance().getDataSource().getConnection();
                var statement = con.prepareStatement("INSERT INTO chunkclaim (uuid, world, x, z) values (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, owner.toString());
                statement.setString(2, getWorld());
                statement.setInt(3, getChunkX());
                statement.setInt(4, getChunkZ());
                statement.executeUpdate();

                try(ResultSet set = statement.getGeneratedKeys()) {
                    if(!set.next()) return null;
                    id = set.getInt(1);
                }
            } catch (SQLException e) {
                return null;
            }
            return this;
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
                var statement = con.prepareStatement("DELETE FROM trustedplayer WHERE claim_id = ? AND uuid = ?")) {
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

    public String getWorld() {
        return chunkKey.world();
    }

    public int getChunkX() {
        return chunkKey.chunkX();
    }

    public int getChunkZ() {
        return chunkKey.chunkZ();
    }

    public UUID[] getTrustedPlayers() {
        return trustedPlayers;
    }

}
