package de.breakcraft.proxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.breakcraft.proxy.MojangAPI;
import de.breakcraft.proxy.ProxyPlugin;
import de.breakcraft.proxy.db.BanEntry;
import de.breakcraft.proxy.db.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Unban implements SimpleCommand {

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player player)) return;
        String[] args = invocation.arguments();

        if(args.length != 1) {
            String msg = "Bitte gebe" + (args.length > 1 ? " nur" : "") + " einen Spieler an !";
            player.sendMessage(Component.text(msg).color(NamedTextColor.RED));
            return;
        }

        Optional<UUID> uuid = MojangAPI.getUUIDByUsername(args[0]);
        if(uuid.isEmpty()) {
            player.sendMessage(Component.text(args[0] + " ist ein ungültiger Spielername !").color(NamedTextColor.RED));
            return;
        }

        BanEntry entry = DatabaseManager.get().getActiveBans().get(uuid.get());
        if(entry == null) {
            player.sendMessage(Component.text("Für diesen Spieler gibt es keinen aktiven Ban !").color(NamedTextColor.RED));
            return;
        }

        DatabaseManager.get().removeBan(entry).thenAccept(succeed -> {
            String msg = succeed ? "Ban wurde aufgehoben !" : "Fehler beim Ausführen des Befehls !";
            player.sendMessage(Component.text(msg).color(NamedTextColor.RED));
        });


    }

    @Override
    public List<String> suggest(Invocation invocation) {
        return SimpleCommand.super.suggest(invocation);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        if(!(invocation.source() instanceof Player player)) return true;
        return player.hasPermission("breakcraft.unban");
    }

}
