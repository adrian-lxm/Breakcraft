package de.breakcraft.proxy;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import de.breakcraft.proxy.commands.Ban;
import de.breakcraft.proxy.db.BanEntry;
import de.breakcraft.proxy.db.DatabaseManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Listeners {
    private final ProxyServer server;
    private final ChannelIdentifier identifier;

    public Listeners(ProxyServer server, ChannelIdentifier identifier) {
        this.server = server;
        this.identifier = identifier;
    }

    @Subscribe
    public void onProxyLogin(LoginEvent e) {
        Player p = e.getPlayer();

        var entries = DatabaseManager.get().getActiveBans();

        BanEntry entry = entries.get(p.getUniqueId());
        if(entry == null) return;
        if(!entry.isActive()) {
            entries.remove(entry.getUuid());
            return;
        }

        e.setResult(ResultedEvent.ComponentResult.denied(Ban.createBanMessage(entry)));
    }

    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        if(event.getPreviousServer().isPresent()) return;

        MiniMessage mm = MiniMessage.miniMessage();

        Component username = Component.text(event.getPlayer().getUsername());
        TagResolver resolver = TagResolver.resolver("spieler", Tag.inserting(username));

        Component msg = mm.deserialize("<yellow>[<green>+</green>] <spieler> </yellow>", resolver);

        server.getScheduler().buildTask(ProxyPlugin.get(), () -> {
            try {
                ByteArrayOutputStream boas = new ByteArrayOutputStream();
                DataOutputStream out = new DataOutputStream(boas);
                out.writeUTF("PlayerCount");
                out.writeUTF("ALL");
                out.writeInt(server.getPlayerCount());
                for(RegisteredServer registeredServer : server.getAllServers()) {
                    registeredServer.sendPluginMessage(identifier, boas.toByteArray());
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).delay(750, TimeUnit.MILLISECONDS).schedule();

        for(Player player : server.getAllPlayers())
            player.sendMessage(msg);

    }

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        MiniMessage mm = MiniMessage.miniMessage();

        Component username = Component.text(event.getPlayer().getUsername());
        TagResolver resolver = TagResolver.resolver("spieler", Tag.inserting(username));

        Component msg = mm.deserialize("<yellow>[<red>-</red>] <spieler> </yellow>", resolver);
        try {
            ByteArrayOutputStream boas = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(boas);
            out.writeUTF("PlayerCount");
            out.writeUTF("ALL");
            out.writeInt(server.getPlayerCount());
            for(RegisteredServer registeredServer : server.getAllServers()) {
                registeredServer.sendPluginMessage(identifier, boas.toByteArray());
                registeredServer.getPlayersConnected().forEach(player -> player.sendMessage(msg));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
