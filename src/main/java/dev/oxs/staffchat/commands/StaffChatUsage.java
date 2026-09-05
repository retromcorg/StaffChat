package dev.oxs.staffchat.commands;

import dev.oxs.staffchat.StaffChat;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class StaffChatUsage implements CommandExecutor {

    private StaffChat plugin;

    public StaffChatUsage(Plugin plugin) {
        this.plugin = (StaffChat) plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender.hasPermission("staffchat.use") || commandSender.isOp())) {
            String message = plugin.getLanguage().getMessage("no_permission");
            commandSender.sendMessage(message);
            return true;
        }

        if (strings.length > 0) {
            String message = String.join(" ", strings);

            if (!(commandSender instanceof Player)) {
                plugin.StaffChatMessage("Console", message);
                return true;
            }

            Player player = (Player) commandSender;
            Boolean t = plugin.getToggleStatus(player.getUniqueId());

            if(t) {
                plugin.PublicChatMessage(player, message);
            }
            else {
                plugin.StaffChatMessage(player, message);
            }

        } else {
            String message = plugin.getLanguage().getMessage("staffchat_usage");
            commandSender.sendMessage(message);
            return true;
        }
        return true;
    }



}
