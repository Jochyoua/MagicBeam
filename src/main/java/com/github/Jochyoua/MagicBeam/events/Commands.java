package com.github.Jochyoua.MagicBeam.events;

import com.github.Jochyoua.MagicBeam.BeamMeScottie;
import com.github.Jochyoua.MagicBeam.Utils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Commands implements CommandExecutor {
    public BeamMeScottie plugin;
    public Utils utils;

    public Commands(BeamMeScottie plugin) {
        this.plugin = plugin;
        this.utils = new Utils(plugin);
        Objects.requireNonNull(plugin.getCommand("MagicBeam")).setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!(sender instanceof Player)) {
            utils.send(sender, plugin.getConfig().getString("variables.sorry but only console"));
            return true;
        }
        boolean permission;
        if (plugin.getConfig().getString("settings.permission.modify").equalsIgnoreCase("none"))
            permission = true;
        else
            permission = sender.hasPermission(plugin.getConfig().getString("settings.permission.modify"));
        if (!permission) {
            utils.send(sender, plugin.getConfig().getString("variables.no permission"));
            return true;
        }
        Player player = (Player) sender;
        plugin.reloadConfig();
        if (args.length == 0) { // if just ran the /MagicBeam command
            utils.send(sender, plugin.getConfig().getString("variables.help message"));
        } else {
            switch (args[0].toLowerCase()) {
                default:
                    utils.send(sender, plugin.getConfig().getString("variables.help message"));
                    break;
                case "remove":
                case "delete":
                    if (args.length == 2) {
                        if (plugin.getConfig().isSet("beacons." + args[1])) {
                            plugin.getConfig().set("beacons." + args[1], null);
                            plugin.saveConfig();
                            utils.send(sender, plugin.getConfig().getString("variables.beacon has been removed").replace("%ID%", args[1] + ""));
                        } else {
                            utils.send(sender, plugin.getConfig().getString("variables.no such beacon").replace("%ID%", args[1]));
                        }
                    } else {
                        Block b = utils.isInBeacon(player);
                        if (b != null) {
                            plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX(), null);
                            utils.send(sender, plugin.getConfig().getString("variables.beacon has been removed").replace("%ID%", b.getZ() + "" + b.getX() + ""));
                            plugin.saveConfig();
                        } else {
                            utils.send(sender, plugin.getConfig().getString("variables.no such beacon under user"));
                        }
                    }
                    break;
                case "list":
                case "count":
                    utils.send(sender, plugin.getConfig().getString("variables.info list").replace("%count%", plugin.getConfig().getConfigurationSection("beacons").getKeys(false).size() + ""));
                    for (String str : plugin.getConfig().getConfigurationSection("beacons").getKeys(false)) {
                        String template = plugin.getConfig().getString("variables.info list template");
                        utils.send(sender, template.replace("%ID%", str).replace("%cords%", plugin.getConfig().getString("beacons." + str + ".cords", "0, 0, 0")));
                    }
                    break;
                case "add":
                    if (args.length == 1) {
                        boolean found = false;
                        for (int y = player.getLocation().getBlockY(); y >= 0; y--) {
                            Block b = player.getLocation().getWorld().getBlockAt(player.getLocation().getBlockX(), y, player.getLocation().getBlockZ());
                            if (b.getType() == Material.matchMaterial(Objects.requireNonNull(plugin.getConfig().getString("settings.block to listen for", "BEACON")))) {
                                found = true;
                                utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.created beacon")).replace("%ID%", b.getZ() + "" + b.getX() + ""));
                                utils.send(sender, plugin.getConfig().getString("variables.success message vector X"));
                                utils.send(sender, plugin.getConfig().getString("variables.success message vector Y"));
                                utils.send(sender, plugin.getConfig().getString("variables.success message vector Z"));
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector X", 0);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector Y", 1);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector Z", 0);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector X (holding shift)", 0);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector Y (holding shift)", -.2);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".Vector Z (holding shift)", 0);
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".world", player.getWorld().getName());
                                plugin.getConfig().set("beacons." + b.getZ() + "" + b.getX() + ".cords", b.getX() + ", " + b.getY() + ", " + b.getZ());
                                plugin.saveConfig();
                            }
                        }
                        if (!found) {
                            utils.send(sender, plugin.getConfig().getString("variables.no such beacon under user"));
                        }
                    } else {
                        utils.send(sender, plugin.getConfig().getString("variables.incorrect arguments"));
                    }
                    break;
                case "info":
                    if (args.length == 1) {
                        utils.send(sender, plugin.getConfig().getString("variables.incorrect arguments"));
                    } else {
                        if (plugin.getConfig().isSet("beacons." + args[1])) {
                            String path = "beacons." + args[1];
                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.info message"))
                                    .replace("%ID%", args[1])
                                    .replace("%cords%", plugin.getConfig().getString(path + ".cords", "0, 0, 0"))
                                    .replace("%x%", plugin.getConfig().getString(path + ".Vector X", "0"))
                                    .replace("%y%", plugin.getConfig().getString(path + ".Vector Y", "0"))
                                    .replace("%z%", plugin.getConfig().getString(path + ".Vector Z", "0"))
                                    .replace("%sx%", plugin.getConfig().getString(path + ".Vector X (holding shift)", "0"))
                                    .replace("%sy%", plugin.getConfig().getString(path + ".Vector X (holding shift)", "0"))
                                    .replace("%sz%", plugin.getConfig().getString(path + ".Vector X (holding shift)", "0")));
                        } else {
                            utils.send(sender, plugin.getConfig().getString("variables.no such beacon").replace("%ID%", args[1]));
                        }
                    }
                    break;
                case "set":
                    // /MagicBeam set $ID X / Y / Z or SX / SY / SZ
                    if (args.length == 1) {
                        utils.send(sender, plugin.getConfig().getString("variables.incorrect arguments"));
                    } else {
                        if (plugin.getConfig().isSet("beacons." + args[1])) {
                            if (args.length == 2) {
                                utils.send(sender, plugin.getConfig().getString("variables.incorrect arguments"));
                            }
                            if (args.length > 2) {
                                Double i = 0.0;
                                //successfully set value: "%prefix% successfully set %type% to %value% for %ID%"
                                switch (args[2].toLowerCase()) {
                                    case "x":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }
                                        plugin.getConfig().set("beacons." + args[1] + ".Vector X", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "Vector X")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    case "y":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }
                                        plugin.getConfig().set("beacons." + args[1] + ".Vector Y", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "Vector Y")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    case "z":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }
                                        plugin.getConfig().set("beacons." + args[1] + ".Vector Z", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "Vector Z")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    case "sx":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }
                                        plugin.getConfig().set("beacons." + args[1] + ".Vector X (holding shift)", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "(Shift) Vector X")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    case "sy":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }

                                        plugin.getConfig().set("beacons." + args[1] + ".Vector Y (holding shift)", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "(Shift) Vector Y")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    case "sz":
                                        try {
                                            i = Double.valueOf(args[3]);
                                        } catch (Exception e) {
                                            utils.send(sender, Objects.requireNonNull(plugin.getConfig().getString("variables.incorrect value"))
                                                    .replace("%needed%", "Double")
                                                    .replace("%value%", args[3]));
                                            i = 0.0;
                                        }
                                        plugin.getConfig().set("beacons." + args[1] + ".Vector Z (holding shift)", i);
                                        plugin.saveConfig();
                                        utils.send(sender, plugin.getConfig().getString("variables.successfully set value")
                                                .replace("%type%", "(Shift) Vector Z")
                                                .replace("%value%", i + "")
                                                .replace("%ID%", args[1]));
                                        break;
                                    default:
                                        utils.send(sender, plugin.getConfig().getString("variables.incorrect arguments"));
                                        break;
                                }
                            }
                        } else {
                            utils.send(sender, plugin.getConfig().getString("variables.no such beacon").replace("%ID%", args[1]));
                        }
                    }
                    break;
            }
        }
        return true;
    }
}
