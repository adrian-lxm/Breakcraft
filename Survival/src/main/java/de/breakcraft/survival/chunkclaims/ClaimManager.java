package de.breakcraft.survival.chunkclaims;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.Chunk;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClaimManager {
    private final HashMap<ChunkKey, ChunkClaim> claims = new HashMap<>();

    public void initManager() {
        try(var con = SurvivalPlugin.get().getDataSource().getConnection();
            var statement = con.prepareStatement("SELECT c.*, GROUP_CONCAT(t.uuid) FROM chunkclaim c LEFT JOIN trustedplayer t ON c.id = t.claim_id GROUP BY c.id")) {
            createTables(con);
            ResultSet set = statement.executeQuery();
            while (set.next()) {
                int id = set.getInt(1);
                UUID owner = UUID.fromString(set.getString(2));
                String world = set.getString(3);
                int chunkX = set.getInt(4);
                int chunkZ = set.getInt(5);
                List<UUID> trustedPlayers = new ArrayList<>();
                String trusted = set.getString(6);
                if (trusted != null) {
                    String[] uuids = trusted.split(",");
                    for (String uuid : uuids)
                        trustedPlayers.add(UUID.fromString(uuid));
                }
                var key = new ChunkKey(world, chunkX, chunkZ);
                claims.put(key, new ChunkClaim(id, owner, key, trustedPlayers.toArray(new UUID[0])));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables(Connection con) throws SQLException {
        String createSql = "CREATE TABLE IF NOT EXISTS chunkclaim" +
                "(id INT NOT NULL AUTO_INCREMENT," +
                "uuid VARCHAR(36) NOT NULL," +
                "world VARCHAR(50) NOT NULL," +
                "x INT NOT NULL," +
                "z INT NOT NULL," +
                "PRIMARY KEY (id))";
        var stmt = con.createStatement();
        stmt.execute(createSql);
        createSql = "CREATE TABLE IF NOT EXISTS trustedplayer" +
                "(claim_id INT NOT NULL," +
                "uuid VARCHAR(36) NOT NULL," +
                "FOREIGN KEY (claim_id) REFERENCES chunkclaim(id) ON DELETE CASCADE," +
                "PRIMARY KEY (claim_id, uuid))";
        stmt.execute(createSql);
        stmt.close();
    }

    public Optional<ChunkClaim> getChunkClaim(Chunk chunk) {
        return Optional.ofNullable(claims.get(ChunkKey.fromChunk(chunk)));
    }

    public HashMap<ChunkKey, ChunkClaim> getClaims() {
        return claims;
    }
}
