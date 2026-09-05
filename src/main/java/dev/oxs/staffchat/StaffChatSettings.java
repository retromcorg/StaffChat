package dev.oxs.staffchat;

// thank you johnymuffin

import org.bukkit.util.config.Configuration;

import java.io.File;

public class StaffChatSettings extends Configuration {
    private static StaffChatSettings singleton;

    private StaffChatSettings(StaffChat plugin) {
        super(new File(plugin.getDataFolder(), "settings.yml"));
        this.reload();
    }

    private void write() {
        //Main
        generateConfigOption("config-version", 1);
        //Setting
        generateConfigOption("settings.staffchat-prefix", "&c[StaffChat]&f");
        generateConfigOption("settings.staffchat-toggle-alert", true);
        generateConfigOption("settings.staffchat-use-displayNamesStaffChat", false);
        generateConfigOption("settings.staffchat-use-displayNamesPublicChat", true);
        generateConfigOption("settings.staffchat-publicChatPrefix", "&f<%player%&f> ");
        //Discord Bridge (requires DiscordCore-4)
        generateConfigOption("discord.enabled", false);
        generateConfigOption("discord.channel-id", "none");
        generateConfigOption("discord.discord-prefix", "&5{&9D &dStaffChat&5}&f");
    }

    public void generateConfigOption(String key, Object defaultValue) {
        if (this.getProperty(key) == null) {
            this.setProperty(key, defaultValue);
        }
        final Object value = this.getProperty(key);
        this.removeProperty(key);
        this.setProperty(key, value);
    }

    //Getters Start
    public Object getConfigOption(String key) {
        return this.getProperty(key);
    }

    public String getConfigString(String key) {
        return String.valueOf(getConfigOption(key));
    }

    public Integer getConfigInteger(String key) {
        return Integer.valueOf(getConfigString(key));
    }

    public Long getConfigLong(String key) {
        return Long.valueOf(getConfigString(key));
    }

    public Double getConfigDouble(String key) {
        return Double.valueOf(getConfigString(key));
    }

    public Boolean getConfigBoolean(String key) {
        return Boolean.valueOf(getConfigString(key));
    }


    //Getters End
    public Long getConfigLongOption(String key) {
        if (this.getConfigOption(key) == null) {
            return null;
        }
        return Long.valueOf(String.valueOf(this.getProperty(key)));
    }


    private boolean convertToNewAddress(String newKey, String oldKey) {
        if (this.getString(newKey) != null) {
            return false;
        }
        if (this.getString(oldKey) == null) {
            return false;
        }
        System.out.println("Converting Config: " + oldKey + " to " + newKey);
        Object value = this.getProperty(oldKey);
        this.setProperty(newKey, value);
        this.removeProperty(oldKey);
        return true;

    }


    private void reload() {
        this.load();
        this.write();
        this.save();
    }

    @Deprecated
    public static StaffChatSettings getInstance() {
        if (StaffChatSettings.singleton == null) {
            throw new RuntimeException("A instance of StaffChat hasn't been passed into StaffChatConfig yet.");
        }
        return StaffChatSettings.singleton;
    }

    @Deprecated
    public static StaffChatSettings getInstance(StaffChat plugin) {
        if (StaffChatSettings.singleton == null) {
            StaffChatSettings.singleton = new StaffChatSettings(plugin);
        }
        return StaffChatSettings.singleton;
    }

}
