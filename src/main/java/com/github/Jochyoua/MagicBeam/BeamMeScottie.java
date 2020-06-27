package com.github.Jochyoua.MagicBeam;

import com.github.Jochyoua.MagicBeam.events.Commands;
import com.github.Jochyoua.MagicBeam.events.PlayerEvents;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BeamMeScottie extends JavaPlugin {
    Utils utils;
    @Override
    public void onEnable() {
        this.utils = new Utils(this);
        saveDefaultConfig();
        getConfig().options().header(
                "MagicBeam v" + getDescription().getVersion() + " by Jochyoua\n"
                        + "This is a toggleable swear filter for your players\n"
                        + "Resource: https://www.spigotmc.org/resources/80695/\n"
                        + "Github: https://www.github.com/Jochyoua/MagicBeam/\n"
                        + "\n\nIf you want to disable a permission, just set the permission to none! Also, if you want to set the sounds to be disabled just set that to none too!\nSounds can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\n\nIf you come across any issues please post them here:\nhttps://github.com/Jochyoua/MagicBeam/issues");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (getConfig().getBoolean("settings.metrics")) {
            new Metrics(this);
        }
        if(getConfig().getBoolean("settings.check for updates"))
            if(!utils.isUpToDate()){
                utils.send(Bukkit.getConsoleSender(), getConfig().getString("variables.found update").replace("%version%", utils.getVersion()+""));
            }
        // Time to load in some listener events!! :)
        new PlayerEvents(this);
        //Registering commands
        new Commands(this);
    }
}
