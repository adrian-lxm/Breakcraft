package de.breakcraft.survival.commands;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

public class Regeln implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(!(commandSender instanceof Player p)) return false;

        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setAuthor("§aBreakcraft Team");
        meta.setTitle("§5Breakcraft Survival Guide");
        String page1 = """
                §aEinführung
                
                §aHier hast du eine kleine Kurzfassung zu allen Möglichkeiten hier !
                """;
        String page2 = """
                §aRegeln
                
                
                1. Respekt gegen über jedem
                
                2.Keine Hacks
                
                3. Kein Griefing
                
                4. Die Beanspruchung von Chunks dient zur Sicherung von Bauflächen.
                Dies bedeutet, dass z.B. die Beanspruchung von Großflächen in den meisten Fällen verboten ist.
                Gehe bitte menschlich mit diesem System um, damit auch andere Spieler einen Platz auf dem Server nutzen können.
                
                5. Keine Beledigungen oder andere Arten der Diskriminierung
                """;
        String page3 = """
                §aEinige nützliche Commands
                
                
                §a1. /pawnshop
                Verkaufe Items gegen Ingame Geld
                
                §a2. /chunk
                Beanspruche Chunks und baue in Sicherheit
                """;
        meta.addPage(page1, page2, page3);
        book.setItemMeta(meta);
        p.openBook(book);
        return true;
    }

}
