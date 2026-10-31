package com.vexsoftware.votifier.cmd;

import com.vexsoftware.votifier.OyvotiferBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class NVReloadCmd implements CommandExecutor {

    private final OyvotiferBukkit plugin;

    public NVReloadCmd(OyvotiferBukkit plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("oyvotifer.reload")) {
            sender.sendMessage(ChatColor.GRAY + "Reloading oyvotifer...");
            if (plugin.reload()) {
                sender.sendMessage(ChatColor.DARK_GREEN + "oyvotifer has been reloaded!");
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "Looks like there was a problem reloading oyvotifer, check the console!");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "You do not have permission to do this!");
        }
        return true;
    }
}
