package de.breakcraft.proxy.commands;

import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.breakcraft.proxy.ProxyPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;

public class Lobby implements SimpleCommand {
    private final ProxyServer server;

    public Lobby() {
        server = ProxyPlugin.get().getServer();
    }

    @Override
    public void execute(Invocation invocation) {
        if(!(invocation.source() instanceof Player player)) return;

        Optional<RegisteredServer> lobby = server.getServer("lobby");
        if(lobby.isEmpty()) {
            player.sendMessage(Component.text("Lobby ist derzeit nicht erreichbar !").color(NamedTextColor.RED));
            return;
        }

        player.createConnectionRequest(lobby.get()).connect();

    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true;
    }

}
