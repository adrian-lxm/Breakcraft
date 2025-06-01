package de.breakcraft.survival.commands;

import de.breakcraft.survival.SurvivalPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;

public class Guide implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player p)) return false;

        var plugin = SurvivalPlugin.get();
        var pathDirector = Paths.get(plugin.getDataFolder().toURI()).resolve("bookPages");
        CompletableFuture.supplyAsync(() -> {
            try(var stream = Files.list(pathDirector)) {
                var files = stream.sorted().toArray(Path[]::new);
                var pages = new String[files.length];
                for(int i = 0; i < files.length; i++) {
                    String page = Files.readString(files[i]);
                    pages[i] = page.length() <= 1024 ? page : page.substring(0, 1024);
                }
                return pages;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).thenAccept((pages) -> Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
            BookMeta meta = (BookMeta) book.getItemMeta();
            for(String page : pages)
                meta.addPage(page);
            book.setItemMeta(meta);
            p.openBook(book);
        }));
        return true;
    }

}
