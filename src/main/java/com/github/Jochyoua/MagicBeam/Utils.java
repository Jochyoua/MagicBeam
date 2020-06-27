package com.github.Jochyoua.MagicBeam;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class Utils {
    BeamMeScottie plugin;

    public Utils(BeamMeScottie plugin) {
        this.plugin = plugin;
    }

    public String prepare(CommandSender player, String message) {
        message = message.replaceAll("(?i)%prefix%", plugin.getConfig().getString("variables.prefix", "[MagicBeam]"));
        message = message.replaceAll("(?i)%player%", player.getName());
        message = ChatColor.translateAlternateColorCodes('&', message);
        return message;
    }

    public void send(CommandSender player, String message) {
        if ("".equals(message))
            return;
        message = prepare(player, message);
        player.spigot().sendMessage(new TextComponent(message));
    }

    public void debug(String str) {
        if (plugin.getConfig().getBoolean("settings.debug"))
            send(Bukkit.getConsoleSender(), plugin.getConfig().getString("variables.debug template").replace("%message%", str));
    }

    public Double getVersion() {
        String version;
        try {
            version = ((JSONObject) new JSONParser().parse(new Scanner(new URL("https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=80695").openStream()).nextLine())).get("current_version").toString();
        } catch (ParseException | IOException ignored) {
            version = plugin.getDescription().getVersion();
        }
        return Double.parseDouble(version);
    }

    public boolean isUpToDate() {
        return !(Double.parseDouble(plugin.getDescription().getVersion()) < this.getVersion());
    }

    public Block isInBeacon(Player player) {
        Material beacon;
        try {
            beacon = Material.valueOf(plugin.getConfig().getString("settings.block to listen for", "BEACON").toUpperCase());
        } catch (IllegalArgumentException ex) {
            beacon = Material.valueOf("BEACON");
            plugin.getConfig().set("settings.block to listen for", "BEACON");
            plugin.saveConfig();
            send(Bukkit.getConsoleSender(), plugin.getConfig().getString("variables.incorrect name for listen for block").replace("%name%", plugin.getConfig().getString("settings.block to listen for")));
        }
        for (int y = player.getLocation().getBlockY(); y >= 0; y--) {
            Block b = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
            if (b.getType().equals(beacon) && plugin.getConfig().isSet("beacons." + b.getZ() + "" + b.getX()) && player.getWorld().getName().equalsIgnoreCase(plugin.getConfig().getString("beacons." + b.getZ() + "_" + b.getX() + ".world", player.getWorld().getName()))) {
                if (b.getType() == Material.BEACON) {
                    if ((((Beacon) b.getState()).getTier() == 0) && plugin.getConfig().getBoolean("settings.require beacons to be active"))
                        return null;
                }
                return b;
            }

        }
        return null;
    }
}
