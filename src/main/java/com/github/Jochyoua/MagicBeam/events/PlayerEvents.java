package com.github.Jochyoua.MagicBeam.events;

import com.github.Jochyoua.MagicBeam.BeamMeScottie;
import com.github.Jochyoua.MagicBeam.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerEvents implements Listener {

    public BeamMeScottie plugin;
    public Utils utils;
    List<UUID> players = new ArrayList<>();

    public PlayerEvents(BeamMeScottie plugin) {
        this.plugin = plugin;
        this.utils = new Utils(plugin);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }


    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (players.contains(e.getPlayer().getUniqueId()) && plugin.getConfig().getBoolean("settings.prevent players from taking damage")) {
            players.remove(e.getPlayer().getUniqueId());
            if (plugin.getConfig().getBoolean("settings.prevent player kicking while in air"))
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
        Block b = utils.isInBeacon(player);
        if (players.contains(player.getUniqueId()) && player.isOnGround() && b == null) {
            if (!plugin.getConfig().getString("settings.sounds.leave sound").equalsIgnoreCase("none")) {
                try {
                    player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("settings.sounds.leave sound")), 1, 1);
                } catch (Exception ignored) {
                    utils.send(Bukkit.getConsoleSender(), plugin.getConfig().getString("variables.failed to play sound").replace("%type%", "leave sound"));
                }
            }
        }
        if (plugin.getConfig().getString("settings.permission.use beams").equalsIgnoreCase("none"))
            permission = true;
        else
            permission = player.hasPermission(plugin.getConfig().getString("settings.permission.use beams"));
        if (permission) {
            plugin.reloadConfig();
            if (b != null) {
                Vector vel = player.getVelocity();
                if (player.isSneaking() && plugin.getConfig().getBoolean("settings.shift makes players go down")) {
                    vel.setX(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector X (holding shift)", 0));
                    vel.setY(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector Y (holding shift)", -.2));
                    vel.setZ(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector Z (holding shift)", 0));
                } else {
                    vel.setX(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector X", 0));
                    vel.setY(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector Y", 1));
                    vel.setZ(plugin.getConfig().getDouble("beacons." + b.getZ()+""+b.getX() + ".Vector Z", 0));
                }
                if (plugin.getConfig().getBoolean("settings.prevent players from taking damage") && !players.contains(player.getUniqueId())) {
                    utils.debug(player.getName() + " has been added to the no fall list.");
                    players.add(player.getUniqueId());
                    if (plugin.getConfig().getBoolean("settings.prevent player kicking while in air"))
                        player.setAllowFlight(true);
                    // play sounds
                    if (!plugin.getConfig().getString("settings.sounds.enter sound").equalsIgnoreCase("none")) {
                        try {
                            player.playSound(player.getLocation(), Sound.valueOf(plugin.getConfig().getString("settings.sounds.enter sound")), 1, 1);
                        } catch (Exception ignored) {
                            utils.send(Bukkit.getConsoleSender(), plugin.getConfig().getString("variables.failed to play sound").replace("%type%", "enter sound"));
                        }
                    }
                    players.add(player.getUniqueId());
                }
                player.setVelocity(vel);
            }
        }
        if (plugin.getConfig().getBoolean("settings.prevent players from taking damage") && player.isOnGround() && players.contains(player.getUniqueId())) {
            if(utils.isInBeacon(player) == null) {
                players.remove(player.getUniqueId());
                utils.debug(player.getName() + " has been removed from the no fall list.");
                if (plugin.getConfig().getBoolean("settings.prevent player kicking while in air"))
                    player.setAllowFlight(false);
            }
        }
    }
}
