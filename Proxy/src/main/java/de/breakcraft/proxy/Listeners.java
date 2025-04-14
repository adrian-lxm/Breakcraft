package de.breakcraft.proxy;

import com.velocitypowered.api.event.EventTask;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Listeners {
    public static List<Integer> takedPins = new ArrayList<>();
    public static HashMap<ProxiedPlayer, Boolean> verified = new HashMap<>();
    public static List<UUID> joined = new ArrayList<>();
    public static List<UUID> banned = new ArrayList<>();
    private static Random random = new Random();

    @Subscribe
    public EventTask onProxyLogin(LoginEvent e) {
        Player p = e.getPlayer();
        if(e.getReason() == ServerConnectEvent.Reason.JOIN_PROXY) {
            boolean allowed = true;
            try(Connection connection = ProxyPlugin.dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement("select * from Bans where uuid = ?")) {
                preparedStatement.setString(1, String.valueOf(p.getUniqueId()));
                ResultSet set = preparedStatement.executeQuery();
                while(set.next()) {
                    if(!set.getBoolean("passing")) {
                        String disallow = "§5----------------------------- Breakcraft Netzwerk -----------------------------\n\n\n" +
                                "§cDu wurdest gebannt !\n\n" +
                                "§cGrund: §e";
                        String reason = set.getString("reason");
                        double endsUp = set.getDouble("ends-up");
                        if(reason != null) disallow += reason;
                        else disallow += "Nicht angegeben";
                        if(endsUp != 0) {
                            disallow +="\n\n§aAblauf des Bannes: §e" + ConvertMilliSecondsToFormattedDate((long) endsUp);
                        }
                        disallow += "\n\n\n§5----------------------------- Breakcraft Netzwerk -----------------------------";
                        banned.add(p.getUniqueId());
                        e.getPlayer().disconnect(new TextComponent(disallow));
                        allowed = false;
                    }
                }
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
        }
    }

    @Subscribe
    public void onDisconnect(DisconnectEvent e) {
        if(!(banned.contains(e.getPlayer().getUniqueId()))) {
            try(Connection connection = ProxyPlugin.dataSource.getConnection(); PreparedStatement selectStatement = connection.prepareStatement("UPDATE `ServerInfo` SET value = ? WHERE id = ?")) {
                selectStatement.setString(1, String.valueOf(ProxyServer.getInstance().getOnlineCount() - 1));
                selectStatement.setInt(2, 1);
                selectStatement.executeUpdate();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
            ProxyServer.getInstance().broadcast(new TextComponent("§e[§c-§e] " + e.getPlayer().getName()));
        } else banned.remove(e.getPlayer().getUniqueId());
    }

    public static String ConvertMilliSecondsToFormattedDate(long milliSeconds) {
        Instant instant = Instant.ofEpochMilli(milliSeconds);
        ZoneId id = ZoneId.systemDefault();
        ZonedDateTime zdt = ZonedDateTime.ofInstant( instant , id );
        String dateFormat = "dd-MM-yyyy hh:mm a";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(dateFormat);
        return zdt.format(formatter);
    }

}
