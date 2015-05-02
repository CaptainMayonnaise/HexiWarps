package com.hexicraft.warps;

import org.bukkit.Bukkit;
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
public class Warps extends JavaPlugin implements Listener {

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
            getLogger().warning("A database error has occured enabling warps: ");
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
                case "warp":
                    code = warp(player, args);
                    break;
                case "createwarp":
                    code = createWarp(player, args);
                    break;
                case "delwarp":
                    code = delWarp(player, args);
                    break;
                case "mywarps":
                    code = myWarps(player, args);
                    break;
                case "ownedby":
                    code = ownedBy(player, args);
                    break;
                default:
                    code = ReturnCode.INVALID_COMMAND;
            }
        } else {
            code = ReturnCode.NOT_PLAYER;
        }
        if (!code.isSuccess) {
            sender.sendMessage(code.message);
        }
        return code.hasValidSyntax;
    }

    private ReturnCode warp(Player player, String[] args) {
        try {
            if (args.length == 0) {
                return mySQL.listWarps(player);
            } else {
                return mySQL.warp(player, args);
            }
        } catch (SQLException e) {
            return mySqlError(e);
        }
    }

    private ReturnCode createWarp(Player player, String[] args) {
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

    private ReturnCode delWarp(Player player, String[] args) {
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

    private ReturnCode myWarps(Player player, String[] args) {
        try {
            if (args.length == 0) {
                return mySQL.listWarps(player, player.getName());
            } else {
                return mySQL.listWarps(player, args[0]);
            }
        } catch (SQLException e) {
            return mySqlError(e);
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

    private ReturnCode mySqlError(SQLException e) {
        if (e.getErrorCode() == 1064) {
            return ReturnCode.INVALID_SYNTAX;
        } else {
            e.printStackTrace();
            return ReturnCode.MYSQL_ERROR;
        }
    }
}