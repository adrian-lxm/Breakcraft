package de.breakcraft.proxy;

import com.google.common.io.ByteArrayDataInput;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Optional;

public class PluginMessagesListener {
    private final ChannelIdentifier identifier;

    public PluginMessagesListener(ChannelIdentifier identifier) {
        this.identifier = identifier;
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if(!event.getIdentifier().getId().equals(identifier.getId())) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if(!(event.getSource() instanceof ServerConnection connection)) return;
        Player player = connection.getPlayer();

        try {
            ByteArrayDataInput in = event.dataAsDataStream();
            String subchannel = in.readUTF();

            switch (subchannel) {
                case "PlayerCount" -> {
                    ByteArrayOutputStream boas = new ByteArrayOutputStream();
                    DataOutputStream out = new DataOutputStream(boas);
                    out.writeUTF("PlayerCount");
                    String countServer = in.readUTF();
                    int playerCount;
                    if(countServer.equalsIgnoreCase("all"))
                        playerCount = ProxyPlugin.get().getServer().getPlayerCount();
                    else {
                        Optional<RegisteredServer> registeredServer = ProxyPlugin.get().getServer().getServer(countServer);
                        playerCount = registeredServer.map(value -> value.getPlayersConnected().size()).orElse(-1);
                    }
                    out.writeUTF(countServer);
                    out.writeInt(playerCount);

                    if(player.getCurrentServer().isEmpty()) return;
                    player.getCurrentServer().get().sendPluginMessage(identifier, boas.toByteArray());
                }

                case "Connect" -> {
                    String server = in.readUTF();
                    Optional<RegisteredServer> connectServer = ProxyPlugin.get().getServer().getServer(server);
                    if(connectServer.isEmpty()) {
                        player.sendMessage(Component.text("Dieser Server ist derzeit nicht verfügbar !").color(NamedTextColor.RED));
                        break;
                    }
                    player.createConnectionRequest(connectServer.get()).connect().thenAcceptAsync(result -> {
                        if(result.isSuccessful()) return;
                        player.sendMessage(Component.text("Verbindung zum Server derzeit nicht möglich !").color(NamedTextColor.RED));
                        if (result.getReasonComponent().isPresent()) {
                            TextComponent reason = (TextComponent) result.getReasonComponent().get();
                            player.sendMessage(reason.color(NamedTextColor.RED));
                        }
                    });
                }

                default -> {}
            }
        } catch (IOException e) {
            ProxyPlugin.get().getLogger().severe("Error while plugin messaging: " + e.getMessage());
        }
    }

}
