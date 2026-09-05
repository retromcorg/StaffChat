package dev.oxs.staffchat;

import dev.oxs.staffchat.commands.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Logger;

public class StaffChat extends JavaPlugin implements Listener {

    private Logger log;
    private String pluginName;

    private StaffChatLanguage language;

    private static StaffChat plugin;

    private PluginDescriptionFile pdf;

    private HashMap<UUID, Boolean> staffChatToggled = new HashMap<>();

    private DiscordBridge discordBridge;
    //TODO upgrade to Discord Core 5 when support is EOL

    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();

        StaffChatSettings.getInstance(plugin);

        final StaffChatListener sc = new StaffChatListener(plugin, staffChatToggled);
        Bukkit.getPluginManager().registerEvents(sc, plugin);

        language = new StaffChatLanguage(new File(this.getDataFolder(), "language.yml"), false);

        // commmands
        Bukkit.getPluginCommand("StaffChatCommand").setExecutor(new StaffChatCommand(plugin));
        Bukkit.getPluginCommand("StaffChatUsage").setExecutor(new StaffChatUsage(plugin));
        Bukkit.getPluginCommand("StaffChatCheck").setExecutor(new StaffChatCheck(plugin));
        Bukkit.getPluginCommand("StaffChatPublic").setExecutor(new StaffChatPublic(plugin));
        Bukkit.getPluginCommand("StaffChatToggle").setExecutor(new StaffChatToggle(plugin, staffChatToggled));

        pluginName = pdf.getName();

        discordBridge = new DiscordBridge(plugin);
        discordBridge.start();

        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());
    }


    @Override
    public void onDisable() {
        if (discordBridge != null) discordBridge.stop();
        log.info(pluginName + " has been disabled.");
    }

    public void StaffChatMessage(Player sender, String message) {
        String name = StaffChatSettings.getInstance(plugin).getConfigBoolean("settings.staffchat-use-displayNamesStaffChat")
                ? sender.getDisplayName() : sender.getName();
        StaffChatMessage(name, message);
    }

    public void StaffChatMessage(String name, String message) {
        String formatted = plugin.getPluginPrefix() + " " + ChatColor.WHITE + name + ": " + ChatColor.WHITE + printColours(message);
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("staffchat.see") || onlinePlayer.isOp()) {
                onlinePlayer.sendMessage(formatted);
            }
        }
        log.info(ChatColor.stripColor(formatted));
        if (discordBridge != null) discordBridge.sendToDiscord(name, message);
    }

    public void staffChatMessageFromDiscord(String discordUsername, String message) {
        String discordPrefix = printColours(StaffChatSettings.getInstance(plugin).getConfigString("discord.discord-prefix"));
        String formatted = discordPrefix + " " + ChatColor.WHITE + discordUsername + ": " + ChatColor.WHITE + printColours(message);
        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("staffchat.see") || onlinePlayer.isOp()) {
                onlinePlayer.sendMessage(formatted);
            }
        }
        log.info("[StaffChat][Discord] " + discordUsername + ": " + message);
    }

    public void PublicChatMessage(Player sender, String message) {
//        for (Player onlinePlayer : getServer().getOnlinePlayers()) {
//
//            String playerPrefix = StaffChatSettings.getInstance(plugin).getConfigString("settings.staffchat-publicChatPrefix");
//            Boolean useDisplay = StaffChatSettings.getInstance(plugin).getConfigBoolean("settings.staffchat-use-displayNamesPublicChat");
//
//            String replacedString = playerPrefix.replace("%player%", (useDisplay ? sender.getDisplayName() : sender.getName()));
//            onlinePlayer.sendMessage(plugin.printColours(replacedString) + ChatColor.WHITE + plugin.printColours(message));
//        }

        // Send public chat message the same way as the server does

        PlayerChatEvent event = new PlayerChatEvent(sender, message);
        Bukkit.getPluginManager().callEvent(event);

        if(event.isCancelled()) {
            return;
        }

        String formattedMessage = String.format(event.getFormat(), event.getPlayer().getDisplayName(), event.getMessage());
        ((CraftServer) Bukkit.getServer()).getServer().console.sendMessage(formattedMessage); // Log to console
        for (Player recipient : event.getRecipients()) {
            recipient.sendMessage(formattedMessage);
        }
    }

    public boolean getToggleStatus(UUID player) {
        return staffChatToggled.getOrDefault(player, false);
    }

    public String getPluginPrefix() {
        return printColours(StaffChatSettings.getInstance(plugin).getConfigString("settings.staffchat-prefix"));
    }

    public String printColours(String str) {
        return str.replaceAll("(&([4c6f2aeb319d5780]))", "\u00A7$2");
    }

    public StaffChatLanguage getLanguage() {
        return language;
    }


    public StaffChatSettings getConfig() {
        return StaffChatSettings.getInstance(plugin);
    }

}
