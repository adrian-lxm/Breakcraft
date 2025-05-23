package de.breakcraft.survival.chunkclaims;

import org.bukkit.Chunk;

//this record is used for making HashMaps usable, accepting more RAM-usage for faster access
public record ChunkKey(String world, int chunkX, int chunkZ) {

    public static ChunkKey fromChunk(Chunk chunk) {
        return new ChunkKey(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

}
