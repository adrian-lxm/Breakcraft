package de.breakcraft.proxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.breakcraft.proxy.ProxyPlugin;
import de.breakcraft.proxy.db.BanEntry;
import de.breakcraft.proxy.db.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

public class Ban implements SimpleCommand {
    private final ProxyServer server;

    public Ban() {
        this.server = ProxyPlugin.get().getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player player)) return;
        String[] args = invocation.arguments();
        var server = ProxyPlugin.get().getServer();

        if (args.length == 1) {
            Optional<Player> banned = server.getPlayer(args[0]);
            if(banned.isEmpty()) {
                player.sendMessage(Component.text(args[0] + " existiert nicht !").color(NamedTextColor.RED));
                return;
            }

            DatabaseManager.get().addBan(banned.get(), -1, null)
                    .thenAccept(entry -> handleEntry(entry, player));

            return;
        }

        if(args.length == 2) {
            Optional<Player> banned = server.getPlayer(args[0]);
            if(banned.isEmpty()) {
                player.sendMessage(Component.text(args[0] + " existiert nicht !").color(NamedTextColor.RED));
                return;
            }

            char unit = args[1].charAt(args[1].length() - 1);

            long multiplicator;
            switch (unit) {
                case 'd' -> multiplicator = 24L * 60 * 60 * 1000;

                case 'w' -> multiplicator = 7L * 24 * 60 * 60 * 1000;

                case 'm' -> multiplicator = 30L * 24 * 60 * 60 * 1000;

                default -> {
                    player.sendMessage(Component.text(unit + " ist keine Zeiteinheit !").color(NamedTextColor.RED));
                    return;
                }
            }

            try {
                int time = Integer.parseInt(args[1].substring(0, args[1].length() - 1));
                DatabaseManager.get().addBan(banned.get(), (long) time * multiplicator, null)
                        .thenAccept(entry -> handleEntry(entry, player));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Ungültige Zahl !").color(NamedTextColor.RED));
                return;
            }

        }

        if(args.length > 2) {
            Optional<Player> banned = server.getPlayer(args[0]);
            if(banned.isEmpty()) {
                player.sendMessage(Component.text(args[0] + " existiert nicht !").color(NamedTextColor.RED));
                return;
            }

            char unit = args[1].charAt(args[1].length() - 1);

            long multiplicator;
            switch (unit) {
                case 'd' -> multiplicator = 24L * 60 * 60 * 1000;

                case 'w' -> multiplicator = 7L * 24 * 60 * 60 * 1000;

                case 'm' -> multiplicator = 30L * 24 * 60 * 60 * 1000;

                default -> {
                    player.sendMessage(Component.text(unit + " ist keine Zeiteinheit !").color(NamedTextColor.RED));
                    return;
                }
            }

            try {
                int time = Integer.parseInt(args[1].substring(0, args[1].length() - 1));

                String reason = args[2];
                for(int i = 3; i < args.length; i++) {
                    reason += ' ' + args[i];
                }

                DatabaseManager.get().addBan(banned.get(), (long) time * multiplicator, reason)
                        .thenAccept(entry -> handleEntry(entry, player));
            } catch (NumberFormatException e) {
                player.sendMessage(Component.text("Ungültige Zahl !").color(NamedTextColor.RED));
            }

        }

    }

    private void handleEntry(BanEntry entry, Player player) {
        if (entry == null) {
            player.sendMessage(Component.text("Fehler beim Ausführen des Befehls !").color(NamedTextColor.RED));
            return;
        }
        Player banned = server.getPlayer(entry.getUuid()).get();

        banned.disconnect(Ban.createBanMessage(entry));
        player.sendMessage(Component.text(banned.getUsername() + " wurde gebannt !").color(NamedTextColor.GREEN));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if(!(invocation.source() instanceof Player player)) return true;
        return player.hasPermission("breakcraft.ban");
    }

    public static Component createBanMessage(BanEntry entry) {
        final String banMessage = "<dark_purple>----------------------------- Breakcraft Netzwerk -----------------------------</dark_purple>\n\n\n" +
                "<red>Du wurdest gebannt !</red>\n\n" +
                "<red>Grund: <yellow><grund></yellow></red>";
        final String durationMessage = "\n<green>Dauer des Bannes: <yellow><ablauf></yellow></green>";
        final String endMessage = "\n\n\n<dark_purple>----------------------------- Breakcraft Netzwerk -----------------------------</dark_purple>";

        String reason = entry.getReason() != null ? entry.getReason() : "Nicht angegeben";
        String duration = entry.getDuration() != -1 ? durationToDate(entry.getTimestamp() + entry.getDuration()) : "Permanent";

        MiniMessage mm = MiniMessage.miniMessage();
        return mm.deserialize(
                banMessage,
                TagResolver.resolver("grund", Tag.inserting(Component.text(reason)))
        ).append(mm.deserialize(
                durationMessage,
                TagResolver.resolver("ablauf", Tag.inserting(Component.text(duration)))
        )).append(mm.deserialize(endMessage));
    }

    private static String durationToDate(long duration) {
        Instant instant = Instant.ofEpochMilli(duration);
        ZoneId id = ZoneId.systemDefault();
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , id );
        String dateFormat = "dd-MM-yyyy hh:mm:a";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return zdt.format(formatter);
    }

}