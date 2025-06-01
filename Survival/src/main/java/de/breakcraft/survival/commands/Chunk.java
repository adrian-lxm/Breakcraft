package de.breakcraft.survival.commands;

import de.breakcraft.survival.SurvivalPlugin;
import de.breakcraft.survival.chunkclaims.ChunkClaim;
import de.breakcraft.survival.chunkclaims.ChunkKey;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.*;

public class Chunk implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!(sender instanceof Player p)) {
            sender.sendMessage("Das kannst du nur als Spieler");
            return false;
        }

        var plugin = SurvivalPlugin.getInstance();

        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "info" -> {
                        Optional<ChunkClaim> optionalChunk = plugin.getClaimManager().getChunkClaim(p.getLocation().getChunk());
                        if(optionalChunk.isEmpty()) {
                            p.sendMessage("§cDieser Chunk ist nicht beansprucht !");
                            return true;
                        }
                        ChunkClaim claim = optionalChunk.get();
                        p.sendMessage(infoMessage(claim));
                    }

                    case "price" -> {
                        int count = plugin.getClaimManager().getClaims().values().stream()
                                .filter(claim -> claim.isOwner(p.getUniqueId()))
                                .mapToInt(claim -> 1)
                                .sum();
                        String message;
                        if(count != 0) {
                            int price = 1000 + 150 * pow(count-1);
                            message = "§aDeine nächste Chunk-Beanspruchung kostet §e" + price + "€ §a.";
                        } else message = "§aDeine erster Beanspruchung ist gratis!";
                        p.sendMessage(message);
                    }

                    case "claim" -> {
                        var world = p.getWorld();
                        if(world.getEnvironment() != World.Environment.NORMAL) {
                            p.sendMessage("§cDu kannst nur in der Oberwelt Chunks beanspruchen !");
                            return true;
                        }
                        if(p.getLocation().distance(world.getSpawnLocation()) < 250) {
                            p.sendMessage("§cDu musst 250 Blöcke vom Spawn entfernt sein !");
                            return true;
                        }
                        int count = plugin.getClaimManager().getClaims().values().stream()
                                .filter(claim -> claim.isOwner(p.getUniqueId()))
                                .mapToInt(claim -> 1)
                                .sum();
                        int price;
                        if(count != 0) price = 1000 + 150 * pow(count-1);
                        else price = 0;
                        if(plugin.getEconomy().getBalance(p) < price) {
                            p.sendMessage("§cDu hast nicht genug Geld!");
                            return true;
                        }
                        var chunk = p.getLocation().getChunk();
                        if(plugin.getClaimManager().getChunkClaim(chunk).isPresent()) {
                            p.sendMessage("§cDieser Chunk ist bereits beansprucht!");
                            return true;
                        }
                        var key = ChunkKey.fromChunk(chunk);
                        new ChunkClaim(key, p.getUniqueId()).saveToDatabase().thenAccept(claim -> Bukkit.getScheduler().runTask(plugin, () -> {
                            if(claim == null) {
                                p.sendMessage("§cEs gab einen Fehler! Bitte kontaktiere den Administrator");
                                return;
                            }
                            plugin.getEconomy().withdrawPlayer(p, price);
                            plugin.getClaimManager().getClaims().put(key, claim);
                            p.sendMessage("§aDu hast diesen Chunk für §e" + price + "€ beansprucht!");
                        }));
                    }

                    default -> p.sendMessage(helpMessage());

                }


            }

            case 3 -> {
                if(!args[0].equals("trust")) {
                    p.chat("/chunk help");
                    return true;
                }

                boolean add = args[1].equals("add");
                if(!(add || args[1].equals("remove"))) {
                    p.chat("/chunk help");
                    return true;
                }
                var trust = Bukkit.getOfflinePlayer(args[2]);
                if(!trust.hasPlayedBefore()) {
                    p.sendMessage("§cDieser Spieler war noch nie auf dem Server!");
                    return true;
                }
                Optional<ChunkClaim> optionalClaim = plugin.getClaimManager().getChunkClaim(p.getLocation().getChunk());
                if(optionalClaim.isEmpty()) {
                    p.sendMessage("§cDieser Chunk ist nicht beansprucht!");
                    return true;
                }
                var claim = optionalClaim.get();
                if(!claim.isOwner(p.getUniqueId())) {
                    p.sendMessage("§cDu bist nicht Besitzer dieses Chunks!");
                    return true;
                }
                var scheduler = Bukkit.getScheduler();
                if(add) {
                    if(claim.isTrusted(trust.getUniqueId())) {
                        p.sendMessage("§cDiesem Spieler wird bereits vertraut!");
                        return true;
                    }
                    claim.addTrusted(trust.getUniqueId()).thenAccept((success) -> scheduler.runTask(plugin, () -> {
                        if(!success) p.sendMessage("§cEs gab einen Fehler! Bitte kontaktiere den Administrator!");
                        else p.sendMessage("§e" + trust.getName() +" §awird nun auf diesem Chunk vertraut!");
                    }));
                } else {
                    if(!claim.isTrusted(trust.getUniqueId())) {
                        p.sendMessage("§cDieser Spieler ist nicht auf der Vertrauensliste!");
                        return true;
                    }
                    claim.removeTrusted(trust.getUniqueId()).thenAccept((success) -> scheduler.runTask(plugin, () -> {
                        if(!success) p.sendMessage("§cEs gab einen Fehler! Bitte kontaktiere den Administrator!");
                        else p.sendMessage("§e" + trust.getName() +" §cwird nun nicht mehr auf diesem Chunk vertraut!");
                    }));
                }

            }

            default -> p.chat("/chunk help");
        }

        return true;
    }

    private int pow(int a) {
        return a*a;
    }

    private String infoMessage(ChunkClaim claim) {
        String info = """
                §a-------------------------------------------
                
                §aInfos zum aktuellen Chunk
                
                §aBesitzer: §e%s
                §aVertraute Spieler:%s
                
                §a-------------------------------------------
                """;
        OfflinePlayer owner = Bukkit.getOfflinePlayer(claim.getOwner());
        String trusted;
        if(claim.getTrustedPlayers().length != 0) {
            trusted = "";
            for(UUID id : claim.getTrustedPlayers()) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(id);
                trusted += "\n   §a- §e" + player.getName();
            }
        } else trusted = "\n   §eNone";
        return String.format(info, owner.getName(), trusted);
    }

    private String helpMessage() {
        return """
                §a-------------------------------------------
                
                §aAlle Command Möglichkeiten für §e/chunk
                
                §e/chunk info §a- Gibt dir Infos zum aktuellen Chunk
                §e/chunk claim §a- Beansprucht den Chunk zu deinem aktuellen Preis
                §e/chunk price §a- Zeigt dir deinen aktuellen Preis für eine Beanspruchung an
                §e/chunk trust [add|remove] [spieler]
                
                [] = benötigtes Argument
                §a-------------------------------------------""";
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        List<String> firstChoices = Arrays.asList("help", "info", "claim", "price");
        if(args.length > 0) {
            List<String> complete = new ArrayList<>();
            for(String i : firstChoices) {
                if(i.startsWith(args[0])) complete.add(i);
            }
            return complete;
        } else return firstChoices;
    }

}
