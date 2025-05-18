package de.breakcraft.survival.listeners;

import de.breakcraft.survival.SurvivalPlugin;
import de.breakcraft.survival.utils.ChunkClaim;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.List;

public class ChunkClaimListeners implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent e) {
        if(e.getAction() == Action.LEFT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if(SurvivalPlugin.ccm.isChunkClaimed(block.getChunk())) {
                ChunkClaim claim = SurvivalPlugin.ccm.getClaimByChunk(block.getChunk());
                if(!(claim.isTrusted(e.getPlayer()))) {
                    if(!(claim.owner.equals(e.getPlayer().getUniqueId()))) {
                        if(!(e.getPlayer().hasPermission("breakcraft.chunks.others"))) e.setCancelled(true);
                    }
                }
            }
        } else if(e.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = e.getClickedBlock();
            if(SurvivalPlugin.ccm.isChunkClaimed(block.getChunk())) {
                ChunkClaim claim = SurvivalPlugin.ccm.getClaimByChunk(block.getChunk());
                if(!(claim.isTrusted(e.getPlayer()))) {
                    if(!(claim.owner.equals(e.getPlayer().getUniqueId()))) {
                        if(!(e.getPlayer().hasPermission("breakcraft.chunks.others"))) e.setCancelled(true);
                    }
                }
            }
        } else if(e.getAction() == Action.PHYSICAL) {
            if(SurvivalPlugin.ccm.isChunkClaimed(e.getPlayer().getLocation().getChunk())) {
                ChunkClaim claim = SurvivalPlugin.ccm.getClaimByChunk(e.getPlayer().getLocation().getChunk());
                if(!(claim.isTrusted(e.getPlayer()))) {
                    if(!(claim.owner.equals(e.getPlayer().getUniqueId()))) {
                        if(!(e.getPlayer().hasPermission("breakcraft.chunks.others"))) e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent e) {
        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
            Player damager = (Player) e.getDamager();
            Player p = (Player) e.getEntity();
            if(SurvivalPlugin.ccm.isChunkClaimed(p.getLocation().getChunk())) {
                ChunkClaim claim = SurvivalPlugin.ccm.getClaimByChunk(p.getLocation().getChunk());
                if(!(claim.isTrusted(damager) || claim.owner.equals(damager.getUniqueId()))) {
                    if(!(damager.hasPermission("breakcraft.chunks.others"))) {
                        if(claim.isTrusted(p)) e.setCancelled(true);
                        if(claim.owner.equals(p.getUniqueId())) e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(EntityExplodeEvent e) {
        List<Block> blocks = e.blockList();
        for(Block block: blocks) {
            if(SurvivalPlugin.ccm.isChunkClaimed(block.getChunk())) {
                e.setCancelled(true);
            }
        }
    }

}
