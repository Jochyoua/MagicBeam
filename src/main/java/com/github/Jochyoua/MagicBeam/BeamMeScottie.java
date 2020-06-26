package com.github.Jochyoua.MagicBeam;

import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BeamMeScottie extends JavaPlugin {
    private static BeamMeScottie plugin;
    List<UUID> players = new ArrayList<>();

    private static BeamMeScottie getInstance() {
        return plugin;
    }

    public Block isInBeacon(Player player) {
        for (int y = player.getLocation().getBlockY(); y >= 0; y--) {
            Block b = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
            Material beacon;
            try {
                beacon = Material.valueOf(getConfig().getString("settings.block to listen for", "BEACON").toUpperCase());
            } catch (IllegalArgumentException ex) {
                beacon = Material.valueOf("BEACON");
                getConfig().set("settings.block to listen for", "BEACON");
                saveConfig();
                send(Bukkit.getConsoleSender(), getConfig().getString("variables.incorrect name for listen for block").replace("%name%", getConfig().getString("settings.block to listen for")));
            }
            if (b.getType().equals(beacon) && getConfig().isSet("beacons." + b.getX())) {
                if (b.getType() == Material.BEACON) {
                    if ((((Beacon) b.getState()).getTier() == 0) && getConfig().getBoolean("settings.require beacons to be active"))
                        return null;
                }
                return b;
            }

        }
        return null;
    }

    @Override
    public void onEnable() {
        plugin = this;
        saveDefaultConfig();
        getConfig().options().header(
                "MagicBeam v" + getDescription().getVersion() + " by Jochyoua\n"
                        + "This is a toggleable swear filter for your players\n"
                        + "Resource: https://www.spigotmc.org/resources/54115/\n"
                        + "Github: https://www.github.com/Jochyoua/MagicBeam/\n"
                        + "\n\nIf you want to disable use permission, just set the permission to none! Also, if you want to set the sounds to be disabled just set that to none too!\nSounds can be found here: https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html\n\nDisable protection from fall damage by changing settings,protect users from damage time to 0");
        getConfig().options().copyDefaults(true);
        saveConfig();
        if (getConfig().getBoolean("settings.metrics")) {
            new Metrics(this);
        }
        // Time to load in some listener events!! :)
        getServer().getPluginManager().registerEvents(new Listener() {
            final PotionEffect potion = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
            /*@EventHandler
            public void fallDamage(EntityDamageEvent e) {
                if (!(e.getEntity() instanceof Player) || !getConfig().getBoolean("settings.prevent players from taking damage"))
                    return;
                Player player = (Player) e.getEntity();
                if (players.contains(player.getUniqueId()) || isInBeacon(player) != null) {
                        player.setAllowFlight(false);
                    debug("Cancelled fall damage for player " + player.getName() + " and removed from no fall list");
                    e.setCancelled(true);
                    players.remove(player.getUniqueId());
                }
            }*/

            @EventHandler
            public void onPlayerLeave(PlayerQuitEvent e) {
                Player player = e.getPlayer();
                if (players.contains(e.getPlayer().getUniqueId()) && getConfig().getBoolean("settings.prevent players from taking damage")) {
                    players.remove(e.getPlayer().getUniqueId());
                    if (getConfig().getBoolean("settings.prevent player kicking while in air"))
                        player.setAllowFlight(false);
                }
            }

            @EventHandler
            public void entityDamage(EntityDamageEvent e) {
                if (e.getEntity() instanceof org.bukkit.entity.Player && e.getCause().equals(EntityDamageEvent.DamageCause.FALL) && players.contains(e.getEntity().getUniqueId())) {
                    e.setCancelled(true);
                }
            }

            @EventHandler
            public void onPlayerMove(PlayerMoveEvent e) {
                Player player = e.getPlayer();
                boolean permission;
                if (players.contains(player.getUniqueId()) && player.isOnGround() && isInBeacon(player) == null) {
                    debug(player.getName() + " has been removed from the no fall list.");
                    players.remove(player.getUniqueId());
                    if (getConfig().getBoolean("settings.prevent player kicking while in air"))
                        player.setAllowFlight(false);
                    if (!getConfig().getString("settings.sounds.player leave sound").equalsIgnoreCase("none")) {
                        try {
                            player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("settings.sounds.leave sound")), 1, 1);
                        } catch (Exception ignored) {
                            send(Bukkit.getConsoleSender(), getConfig().getString("variables.failed to play sound").replace("%type%", "leave sound"));
                        }
                    }
                }
                if (getConfig().getString("settings.permission.use beams").equalsIgnoreCase("none"))
                    permission = true;
                else
                    permission = player.hasPermission(getConfig().getString("settings.permission.use beams"));
                if (permission) {
                    reloadConfig();
                    if (isInBeacon(player) != null) {
                        Block b = isInBeacon(player);
                        Vector vel = player.getVelocity();
                        if (player.isSneaking() && getConfig().getBoolean("settings.shift makes players go down")) {
                            vel.setX(getConfig().getDouble("beacons." + b.getX() + ".Vector X (holding shift)", 0));
                            vel.setY(getConfig().getDouble("beacons." + b.getX() + ".Vector Y (holding shift)", -.2));
                            vel.setZ(getConfig().getDouble("beacons." + b.getX() + ".Vector Z (holding shift)", 0));
                        } else {
                            vel.setX(getConfig().getDouble("beacons." + b.getX() + ".Vector X", 0));
                            vel.setY(getConfig().getDouble("beacons." + b.getX() + ".Vector Y", 1));
                            vel.setZ(getConfig().getDouble("beacons." + b.getX() + ".Vector Z", 0));
                        }
                        if (getConfig().getBoolean("settings.prevent players from taking damage") && !players.contains(player.getUniqueId())) {
                            debug(player.getName() + " has been added to the no fall list.");
                            players.add(player.getUniqueId());
                            if (getConfig().getBoolean("settings.prevent player kicking while in air"))
                                player.setAllowFlight(true);
                            // play sounds
                            if (!getConfig().getString("settings.sounds.player enter sound").equalsIgnoreCase("none")) {
                                try {
                                    player.playSound(player.getLocation(), Sound.valueOf(getConfig().getString("settings.sounds.enter sound")), 1, 1);
                                } catch (Exception ignored) {
                                    send(Bukkit.getConsoleSender(), getConfig().getString("variables.failed to play sound").replace("%type%", "enter sound"));
                                }
                            }
                            players.add(player.getUniqueId());
                        }
                        player.setVelocity(vel);
                    }
                }
            }
        }, this);
        //Registering commands
        getCommand("MagicBeam").setExecutor(new CommandExecutor() {
            public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
                if (args.length == 0) {
                    send(sender, getConfig().getString("variables.incorrect arguments"));
                    return true;
                }
                if (!(sender instanceof Player)) {
                    send(sender, getConfig().getString("variables.sorry but only console"));
                    return true;
                }
                reloadConfig();
                if (args.length == 1) {
                    if (sender.hasPermission("MagicBeam.modify")) {
                        Player player = (Player) sender;
                        if (args[0].equalsIgnoreCase("add")) {
                            boolean found = false;
                            for (int y = player.getLocation().getBlockY(); y >= 0; y--) {
                                Block b = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
                                if (b.getType() == Material.matchMaterial(getConfig().getString("settings.block to listen for", "BEACON"))) {
                                    found = true;
                                    send(sender, "Found beacon block at Y" + y + "!");
                                    send(sender, getConfig().getString("variables.success message vector X"));
                                    send(sender, getConfig().getString("variables.success message vector Y"));
                                    send(sender, getConfig().getString("variables.success message vector Z"));
                                    getConfig().set("beacons." + b.getX() + ".Vector X", 0);
                                    getConfig().set("beacons." + b.getX() + ".Vector Y", 1);
                                    getConfig().set("beacons." + b.getX() + ".Vector Z", 0);
                                    getConfig().set("beacons." + b.getX() + ".Vector X (holding shift)", 0);
                                    getConfig().set("beacons." + b.getX() + ".Vector Y (holding shift)", -.2);
                                    getConfig().set("beacons." + b.getX() + ".Vector Z (holding shift)", 0);
                                    saveConfig();
                                }
                            }
                            if (!found) {
                                send(sender, getConfig().getString("variables.no such beacon under user"));
                                return true;
                            }
                        } else if (args[0].equalsIgnoreCase("remove")) {
                            boolean found = false;
                            for (int y = player.getLocation().getBlockY(); y >= 0; y--) {
                                Block b = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
                                if (b.getType() == Material.matchMaterial(getConfig().getString("settings.block to listen for", "BEACON"))) {
                                    if (getConfig().isSet("beacons." + b.getX())) {
                                        found = true;
                                        getConfig().set("beacons." + b.getX(), null);
                                        send(sender, getConfig().getString("variables.beacon has been removed").replace("%x%", b.getX() + ""));
                                        saveConfig();
                                    }
                                }
                            }
                            if (!found) {
                                send(sender, getConfig().getString("variables.no such beacon under user"));
                                return true;
                            }
                        }
                    } else {
                        send(sender, getConfig().getString("variables.no permission"));
                    }
                } else {
                    if (args[0].equalsIgnoreCase("remove")) {
                        Integer i = 0;
                        try {
                            i = Integer.parseInt(args[1]);
                        } catch (Exception e) {
                            send(sender, getConfig().getString("variables.invalid number for beacon"));
                            return true;
                        }
                        if (getConfig().isSet("beacons." + i)) {
                            getConfig().set("beacons." + i, null);
                            send(sender, getConfig().getString("variables.beacon has been removed").replace("%x%", i + ""));
                            saveConfig();
                        } else {
                            send(sender, getConfig().getString("variables.no such beacon").replace("%x%", i + ""));
                        }
                    }
                }
                return true;
            }
        });
    }

    public String prepare(CommandSender player, String message) {
        message = message.replaceAll("(?i)%prefix%", getConfig().getString("variables.prefix", "[MagicBeam]"));
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
}
