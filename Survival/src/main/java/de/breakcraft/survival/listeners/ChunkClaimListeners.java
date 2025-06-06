package de.breakcraft.survival.listeners;

import de.breakcraft.survival.SurvivalPlugin;
import de.breakcraft.survival.chunkclaims.ChunkClaim;
import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ChunkClaimListeners implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent e) {
        var claimManager = SurvivalPlugin.get().getClaimManager();
        Player p = e.getPlayer();
        Optional<ChunkClaim> optionalClaim;

        switch (e.getAction()) {
            case LEFT_CLICK_BLOCK, RIGHT_CLICK_BLOCK -> {
                Block block = e.getClickedBlock();
                optionalClaim = claimManager.getChunkClaim(block.getChunk());
            }

            case PHYSICAL -> optionalClaim = claimManager.getChunkClaim(p.getLocation().getChunk());

            default -> {
                return;
            }
        }

        if(optionalClaim.isEmpty()) return;
        var claim = optionalClaim.get();
        if(claim.isTrusted(p.getUniqueId()) || claim.getOwner().equals(p.getUniqueId())) return;
        if(!p.hasPermission("breakcraft.chunks.others")) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player damager && e.getEntity() instanceof Player p)) return;
        var optionalClaim = SurvivalPlugin.get().getClaimManager().getChunkClaim(p.getLocation().getChunk());
        if(optionalClaim.isEmpty()) return;
        var claim = optionalClaim.get();
        if(claim.isTrusted(damager.getUniqueId()) || claim.getOwner().equals(damager.getUniqueId())) return;
        if(damager.hasPermission("breakcraft.chunks.others")) return;
        if(claim.isTrusted(p.getUniqueId()) || claim.isOwner(p.getUniqueId())) e.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(EntityExplodeEvent e) {
        Set<Chunk> chunks = e.blockList().stream()
                .map(Block::getChunk)
                .collect(Collectors.toSet());
        var claimManager = SurvivalPlugin.get().getClaimManager();
        for(var chunk : chunks) {
            var optionalClaim = claimManager.getChunkClaim(chunk);
            if(optionalClaim.isEmpty()) continue;
            e.setCancelled(true);
            break;
        }
    }

}
