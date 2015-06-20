package com.hexicraft.hexiwarps;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public class Main extends JavaPlugin implements Listener {

    private PluginConfiguration config = new PluginConfiguration(this);
    private MySQLInterface mySQL;

    @Override
    public void onDisable() {
        getLogger().info("Warps disabled.");
    }

    @Override
    public void onEnable() {
        mySQL = new MySQLInterface(
                config.getString("MySQL.address"),
                config.getString("MySQL.port"),
                config.getString("MySQL.database"),
                config.getString("MySQL.user"),
                config.getString("MySQL.pass"));
        try {
            mySQL.tryCreateTable();
            getLogger().info("Warps enabled.");
        } catch (SQLException e) {
            getLogger().warning("A database error has occured enabling hexiwarps: ");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        ReturnCode code;

        if (sender instanceof Player) {
            Player player = (Player) sender;
            switch (cmd.getName().toLowerCase()) {
                case "hexiwarps":
                    code = hexiwarps(player);
                    break;
                case "warp":
                    code = warp(player, args);
                    break;
                case "setwarp":
                    code = setwarp(player, args);
                    break;
                case "delwarp":
                    code = delwarp(player, args);
                    break;
                case "ownedby":
                    code = ownedBy(player, args);
                    break;
                case "listwarps":
                    code = listwarps(player, args);
                    break;
                case "playerswarps":
                    code = playerswarps(player, args);
                    break;
                default:
                    code = ReturnCode.INVALID_COMMAND;
            }
        } else {
            code = ReturnCode.NOT_PLAYER;
        }

        if (code != null && code.hasMessage()) {
            // Send the resulting message to the sender
            sender.sendMessage(ChatColor.RED + code.getMessage(cmd));
        }
        return true;
    }

    private ReturnCode hexiwarps(Player player) {
        player.sendMessage(ChatColor.DARK_GRAY + "- - - " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.WHITE + " HexiWarps " +
                ChatColor.RED + "⬢" + ChatColor.GOLD + "⬢" + ChatColor.DARK_RED + "⬢" +
                ChatColor.DARK_GRAY + " - - -");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/warp <warp>" + ChatColor.WHITE +
                " - Teleport to a warp location");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/setwarp <warp>" + ChatColor.WHITE +
                " - Creates a warp location");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/delwarp <warp>" + ChatColor.WHITE +
                " - Deletes a warp location");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/ownedby <warp>" + ChatColor.WHITE +
                " - Find the owner of a warp");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/listwarps [page]" + ChatColor.WHITE +
                " - Lists all of the warps");
        player.sendMessage(ChatColor.DARK_GRAY + "- " + ChatColor.GOLD + "/playerswarps <player> [page]" + ChatColor.WHITE +
                " - List a player's warps");
        return ReturnCode.SUCCESS;
    }

    private ReturnCode warp(Player player, String[] args) {
        if (args.length < 1) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else {
            try {
                return mySQL.warp(player, args);
            } catch (SQLException e) {
                return mySqlError(e);
            }
        }
    }

    private ReturnCode setwarp(Player player, String[] args) {
        if (args.length < 1) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else {
            try {
                return mySQL.createWarp(player, args);
            } catch (SQLException e) {
                return mySqlError(e);
            }
        }
    }

    private ReturnCode delwarp(Player player, String[] args) {
        if (args.length < 1) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else {
            try {
                return mySQL.delWarp(player, args);
            } catch (SQLException e) {
                return mySqlError(e);
            }
        }
    }

    private ReturnCode ownedBy(Player player, String[] args) {
        if (args.length < 1) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else {
            try {
                return mySQL.ownedBy(player, args);
            } catch (SQLException e) {
                return mySqlError(e);
            }
        }
    }

    private ReturnCode listwarps(Player player, String[] args) {
        try {
            if (args.length == 0) {
                return mySQL.listWarps(player, 1);
            } else {
                return mySQL.listWarps(player, Integer.parseInt(args[0]));
            }
        } catch (SQLException e) {
            return mySqlError(e);
        } catch (NumberFormatException e) {
            return ReturnCode.INVALID_ARGUMENT;
        }
    }

    private ReturnCode playerswarps(Player player, String[] args) {
        if (args.length < 1) {
            return ReturnCode.TOO_FEW_ARGUMENTS;
        } else {
            try {
                if (args.length == 1) {
                    return mySQL.listWarps(player, args[0], 1);
                } else {
                    return mySQL.listWarps(player, args[0], Integer.parseInt(args[1]));
                }
            } catch (SQLException e) {
                return mySqlError(e);
            } catch (NumberFormatException e) {
                return ReturnCode.INVALID_ARGUMENT;
            }
        }
    }

    private ReturnCode mySqlError(SQLException e) {
        if (e.getErrorCode() == 1064) {
            return ReturnCode.INVALID_SYNTAX;
        } else {
            e.printStackTrace();
            return ReturnCode.MYSQL_ERROR;
        }
    }
}