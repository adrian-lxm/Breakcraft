package de.breakcraft.survival.chunkclaims;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.Chunk;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class ClaimManager {
    private final HashMap<ChunkKey, ChunkClaim> claims = new HashMap<>();

    public void initManager() {
        try(var con = SurvivalPlugin.getInstance().getDataSource().getConnection();
            var statement = con.prepareStatement("SELECT c.*, GROUP_CONCAT(t.uuid) FROM chunkclaim c LEFT JOIN trustedplayer t ON c.id = t.claimId GROUP BY c.id")) {
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
                    String raw = set.getString("trusted");
                    String[] uuids = trusted.split(",");
                    for (String uuid : uuids)
                        trustedPlayers.add(UUID.fromString(uuid));
                }
                var key = new ChunkKey(world, chunkX, chunkZ);
                claims.put(key, new ChunkClaim(id, owner, key, (UUID[]) trustedPlayers.toArray()));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Optional<ChunkClaim> getChunkClaim(Chunk chunk) {
        return Optional.ofNullable(claims.get(ChunkKey.fromChunk(chunk)));
    }

    public HashMap<ChunkKey, ChunkClaim> getClaims() {
        return claims;
    }
}
