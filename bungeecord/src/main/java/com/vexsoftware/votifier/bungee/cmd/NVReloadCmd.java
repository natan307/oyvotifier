package com.vexsoftware.votifier.bungee.cmd;

import com.vexsoftware.votifier.bungee.Oyvotifier;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

public class NVReloadCmd extends Command {

    private final Oyvotifier plugin;

    private static final BaseComponent reloadingOyvotifier = new TextComponent("Reloading oyvotifier...");
    private static final BaseComponent oyvotifierHasBeenReloaded = new TextComponent("oyvotifier has been reloaded!");
    private static final BaseComponent problem = new TextComponent("Looks like there was a problem reloading oyvotifier, check the console!");
    private static final BaseComponent permission = new TextComponent("You do not have permission to do this!");

    static {
        reloadingOyvotifier.setColor(ChatColor.GRAY);
        oyvotifierHasBeenReloaded.setColor(ChatColor.DARK_GREEN);
        problem.setColor(ChatColor.DARK_RED);
        permission.setColor(ChatColor.DARK_RED);
    }

    public NVReloadCmd(Oyvotifier plugin) {
        super("pnvreload", "oyvotifier.reload");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender.hasPermission("oyvotifier.reload")) {
            sender.sendMessage(reloadingOyvotifier);
            if (plugin.reload()) {
                sender.sendMessage(oyvotifierHasBeenReloaded);
            } else {
                sender.sendMessage(problem);
            }
        } else {
            sender.sendMessage(permission);
        }
    }
}
