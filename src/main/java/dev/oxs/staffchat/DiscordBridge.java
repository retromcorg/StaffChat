package dev.oxs.staffchat;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.retromc.discordcore.api.DiscordCoreAPI;
import org.retromc.discordcore.v6.DiscordCorePlugin;

import java.util.logging.Logger;

public class DiscordBridge {

    private final StaffChat plugin;
    private final Logger log;
    private DiscordCorePlugin discordCore;
    private String channelId;
    private Object jdaListener;

    public DiscordBridge(StaffChat plugin) {
        this.plugin = plugin;
        this.log = plugin.getServer().getLogger();
    }

    public void start() {
        if (!plugin.getConfig().getConfigBoolean("discord.enabled")) return;

        channelId = plugin.getConfig().getConfigString("discord.channel-id");
        if (channelId == null || channelId.isEmpty() || channelId.equals("none")) {
            log.warning("[StaffChat] discord.enabled is true but discord.channel-id is not set.");
            return;
        }

        org.bukkit.plugin.Plugin dc = Bukkit.getPluginManager().getPlugin("DiscordCore-6");
        if (dc == null || !dc.isEnabled()) {
            log.warning("[StaffChat] DiscordCore-6 not found — Discord bridge disabled.");
            return;
        }

        try {
            discordCore = (DiscordCorePlugin) dc;
            registerJdaListener();
            log.info("[StaffChat] Discord bridge active on channel " + channelId);
        } catch (Exception | NoClassDefFoundError e) {
            log.severe("[StaffChat] Failed to attach JDA listener: " + e.getMessage());
        }
    }

    private void registerJdaListener() {
        final String targetChannel = this.channelId;
        ListenerAdapter listener = new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                if (!event.getChannel().getId().equals(targetChannel)) return;
                if (event.getAuthor().isBot() || event.isWebhookMessage()) return;

                final String username = event.getAuthor().getName();
                final String message = event.getMessage().getContentRaw();

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> plugin.staffChatMessageFromDiscord(username, message));
            }
        };
        getJDA().addEventListener(listener);
        jdaListener = listener;
    }

    public void stop() {
        if (discordCore != null && jdaListener != null) {
            try {
                JDA jda = getJDA();
                if (jda != null) jda.removeEventListener(jdaListener);
            } catch (Exception | NoClassDefFoundError ignored) {}
        }
    }

    public void sendToDiscord(String playerName, String message) {
        if (discordCore == null) return;
        try {
            DiscordCoreAPI.sendMessage(channelId, "**[StaffChat]** " + playerName + ": " + message);
        } catch (RuntimeException e) {
            log.warning("[StaffChat] Failed to send message to Discord: " + e);
        }
    }

    private JDA getJDA() {
        if (discordCore.getDiscordBot() == null) return null;
        return discordCore.getDiscordBot().getJDA();
    }
}
