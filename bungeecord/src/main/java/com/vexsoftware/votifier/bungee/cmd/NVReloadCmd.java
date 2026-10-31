package com.vexsoftware.votifier.bungee.cmd;

import com.vexsoftware.votifier.bungee.Oyvotifer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class NVReloadCmd extends Command {

    private final Oyvotifer plugin;

    private static final BaseComponent reloadingOyvotifer = new TextComponent("Reloading oyvotifer...");
    private static final BaseComponent oyvotiferHasBeenReloaded = new TextComponent("oyvotifer has been reloaded!");
    private static final BaseComponent problem = new TextComponent("Looks like there was a problem reloading oyvotifer, check the console!");
    private static final BaseComponent permission = new TextComponent("You do not have permission to do this!");

    static {
        reloadingOyvotifer.setColor(ChatColor.GRAY);
        oyvotiferHasBeenReloaded.setColor(ChatColor.DARK_GREEN);
        problem.setColor(ChatColor.DARK_RED);
        permission.setColor(ChatColor.DARK_RED);
    }

    public NVReloadCmd(Oyvotifer plugin) {
        super("pnvreload", "oyvotifer.reload");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("oyvotifer.reload")) {
            sender.sendMessage(reloadingOyvotifer);
            if (plugin.reload()) {
                sender.sendMessage(oyvotiferHasBeenReloaded);
            } else {
                sender.sendMessage(problem);
            }
        } else {
            sender.sendMessage(permission);
        }
    }
}
