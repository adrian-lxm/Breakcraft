package de.breakcraft.proxy.Commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import java.util.Collections;

public class Ban extends Command implements TabExecutor {

    public Ban() {
        super("ban");
    }

    final String banMessage = "§5----------------------------- Breakcraft Netzwerk -----------------------------\n\n\n" +
                              "§cDu wurdest gebannt !\n\n" +
                              "§cGrund: §e%" +
                              "\n\n\n§5----------------------------- Breakcraft Netzwerk -----------------------------";


    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer p)) return;

        if (!p.hasPermission("breakcraft.ban")) {
            p.sendMessage(new TextComponent(ChatColor.RED + "Dazu hast du nicht die Rechte !"));
            return;
        }



    }


    @Override
    public Iterable<String> onTabComplete (CommandSender commandSender, String[] args){
        var players = ProxyServer.getInstance().getPlayers()
                .stream().map(ProxiedPlayer::getName);
        if(args.length != 1) return Collections.emptyList();
        players = players.filter(p -> p.startsWith(args[0]));
        return players.toList();
    }

}