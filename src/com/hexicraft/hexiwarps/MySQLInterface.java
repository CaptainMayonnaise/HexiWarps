package com.hexicraft.hexiwarps;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public class MySQLInterface {

    private String address;
    private String port;
    private String dbName;
    private String userName;
    private String password;

    private static final int MAX_PERMISSION = 10;

    public MySQLInterface(String address, String port, String dbName, String userName, String password) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.address = address;
            this.port = port;
            this.dbName = dbName;
            this.userName = userName;
            this.password = password;
            Connection connection = createConnection();
            if (connection != null) {
                connection.close();
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<Warp> executeQuery(String query) throws SQLException {
        try (Connection connection = createConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet result = statement.executeQuery(query)) {
                    ArrayList<Warp> warps = new ArrayList<>();
                    while (result.next()) {
                        warps.add(new Warp(
                                result.getInt("id"),
                                result.getString("name"),
                                result.getDouble("x"),
                                result.getDouble("y"),
                                result.getDouble("z"),
                                result.getFloat("yaw"),
                                result.getFloat("pitch"),
                                result.getString("world"),
                                result.getString("uuid")
                        ));
                    }
                    return warps;
                }
            }
        }
    }

    private Map<String, String> hexiadminQuery(String query) throws SQLException {
        Connection connection = null;
        Map<String, String> playerData = new HashMap<>();
        try {
            connection = createConnection();
            Statement statement = connection.createStatement();

            ResultSet result = statement.executeQuery(query);
            if (result.next()) {
                playerData.put("name", result.getString("name"));
                playerData.put("uuid", result.getString("uuid"));
            }

        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return playerData;
    }

    private Map<String, String> getUuid(String name) throws SQLException {
        return hexiadminQuery("SELECT * FROM HexiAdmin WHERE name LIKE '" + name + "'");
    }

    private Map<String, String> getPlayer(String uuid) throws SQLException {
        return hexiadminQuery("SELECT * FROM HexiAdmin WHERE uuid LIKE '" + uuid + "'");
    }

    private void executeUpdate(String update) throws SQLException {
        Connection connection = createConnection();
        Statement statement = connection.createStatement();
        statement.executeUpdate(update);
        connection.close();
    }

    public Connection createConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + address + ":" + port + "/" + dbName
                + "?autoReconnect=true", userName, password);
    }

    public ReturnCode warp(Player player, String[] args) throws SQLException {
        ArrayList<Warp> results = executeQuery(String.format("SELECT * FROM warps WHERE name LIKE '%s'", args[0]));
        if (!results.isEmpty()) {
            Location warpLocation = new Location(results.get(0).getWorld(), results.get(0).getX(),
                    results.get(0).getY(), results.get(0).getZ(), results.get(0).getYaw(), results.get(0).getPitch());
            player.teleport(warpLocation);
            player.sendMessage(ChatColor.GOLD + "Warping to " + ChatColor.WHITE + args[0] + ChatColor.GOLD + ".");
            return ReturnCode.SUCCESS;
        } else {
            return ReturnCode.WARP_NOT_FOUND;
        }
    }

    /**
     * Determines whether or not the warp is being edited or created, then checks whether the player is able to edit it
     * or if they have reached the maximum warps, and then edits/ creates it based on the player location and the name
     * argument given.
     * @param player The player issuing the command
     * @param args args[0] should be the name of the warp
     * @return Whether or not the creation was successful
     * @throws java.sql.SQLException
     */
    public ReturnCode createWarp(Player player, String[] args) throws SQLException {
        Location location = player.getLocation();
        location.setX(Math.floor(location.getX()) + 0.5);
        location.setY(Math.floor(location.getY()));
        location.setZ(Math.floor(location.getZ()) + 0.5);
        //Normalise the position of the warp to the bottom middle of the block

        if (warpExists(args[0])) {
            if (canEditWarp(player, args[0])) {
                executeUpdate("UPDATE " + dbName + ".warps SET" +
                                " x='" + location.getX() +
                                "', y='" + location.getY() +
                                "', z='" + location.getZ() +
                                "', yaw='" + location.getYaw() +
                                "', pitch='" + location.getPitch() +
                                "', world='" + location.getWorld().getName() +
                                "', uuid ='" + player.getUniqueId() +
                                "', name ='" + args[0] +
                                "' WHERE name ='" + args[0] +
                                "'"
                ); //Update the warp position, owner and/or capitalisation
                player.sendMessage(ChatColor.GOLD + "Warp " + ChatColor.WHITE + args[0] + ChatColor.GOLD + " updated.");
                return ReturnCode.SUCCESS;
            } else {
                return ReturnCode.NOT_OWNER;
            }
        } else {
            if (!reachedMaxWarps(player)) {
                executeUpdate("INSERT INTO " + dbName + ".warps " +
                                "(id, name, x, y, z, yaw, pitch, world, uuid) " +
                                "VALUES (NULL" +
                                ", '" + args[0] +
                                "', '" + location.getX() +
                                "', '" + location.getY() +
                                "', '" + location.getZ() +
                                "', '" + location.getYaw() +
                                "', '" + location.getPitch() +
                                "', '" + location.getWorld().getName() +
                                "', '" + player.getUniqueId() +
                                "')"

                ); //Create the warp
                player.sendMessage(ChatColor.GOLD + "Warp " + ChatColor.WHITE + args[0] + ChatColor.GOLD + " created.");
                return ReturnCode.SUCCESS;
            } else {
                return ReturnCode.MAX_WARPS;
            }
        }
    }

    private boolean warpExists(String name) throws SQLException {
        ArrayList<Warp> results = executeQuery(String.format("SELECT * FROM warps WHERE name LIKE '%s'", name));
        return !results.isEmpty();
    }

    /**
     * Determines whether the player can create any more warps
     * @param player The player who is trying to make a warp
     * @return true if they have reached max warps
     * @throws java.sql.SQLException
     */
    public boolean reachedMaxWarps(Player player) throws SQLException {
        if (player.hasPermission("hexiwarps.mod") || player.hasPermission("hexiwarps.admin")) {
            return false; //Mods and Admins have unlimited warps
        } else {
            int maxWarps = 0;
            for (int i = MAX_PERMISSION; i > 0; i--) { //Check hexiwarps.max.i from top to bottom
                if (player.hasPermission("hexiwarps.max." + i)) {

                    maxWarps = i; //If permission is found, set maxWarps and break out
                    break;
                }
            }
            ArrayList<Warp> results = executeQuery("SELECT * FROM warps WHERE uuid LIKE '" + player.getUniqueId() + "'");
            System.out.println(maxWarps + ", " + results.size());
            return maxWarps <= results.size(); //Compare maxWarps with number of warps owned by player
        }
    }

    public boolean canEditWarp(Player player, String name) throws SQLException {
        ArrayList<Warp> results = executeQuery(String.format("SELECT * FROM warps WHERE uuid LIKE '%s' AND name LIKE '%s'", player.getUniqueId().toString(), name));
        return !results.isEmpty() || player.hasPermission("hexiwarps.mod") || player.hasPermission("hexiwarps.admin");
    }

    public ReturnCode delWarp(Player player, String[] args) throws SQLException {
        if (canEditWarp(player, args[0])) {
            executeUpdate(String.format("DELETE FROM %s.warps WHERE warps.name LIKE '%s' ", dbName, args[0]));
            player.sendMessage(ChatColor.GOLD + "Warp " + ChatColor.WHITE +args[0] + ChatColor.GOLD +" deleted.");
            return ReturnCode.SUCCESS;
        } else {
            return ReturnCode.NOT_OWNER;
        }
    }

    public ReturnCode listWarps(Player player) throws SQLException {
        ArrayList<Warp> results = executeQuery("SELECT * FROM warps ORDER BY name ASC");
        if (!results.isEmpty()) {
            String list = ChatColor.GOLD + "Warps: " + ChatColor.WHITE + results.get(0).getName();
            player.sendMessage(generateList(results, list));
            return ReturnCode.SUCCESS;
        } else {
            return ReturnCode.NO_WARPS_CREATED;
        }
    }

    public ReturnCode listWarps(Player player, String playerName) throws SQLException {
        Map<String, String> playerData = getUuid(playerName);
        ArrayList<Warp> results = executeQuery(String.format("SELECT * FROM warps WHERE uuid LIKE '%s' ORDER BY name ASC", playerData.get("uuid")));
        if (!results.isEmpty()) {
            String list = ChatColor.GOLD + playerData.get("name") + "'s warps: " + ChatColor.WHITE + results.get(0).getName();
            player.sendMessage(generateList(results, list));
            return ReturnCode.SUCCESS;
        } else {
            return ReturnCode.NO_WARPS_PLAYER;
        }
    }

    public String generateList(ArrayList<Warp> results, String list) throws SQLException {
        for (int warp = 1; warp < results.size(); warp++) {
            list += ", " + results.get(warp).getName();
        }
        return list;
    }

    public ReturnCode ownedBy(Player player, String[] args) throws SQLException {
        ArrayList<Warp> results = executeQuery(String.format("SELECT * FROM warps WHERE name LIKE '%s'", args[0]));
        if (!results.isEmpty()) {
            Map<String, String> playerData = getPlayer(results.get(0).getUuid());
            player.sendMessage(ChatColor.GOLD + "Warp '" + args[0] + "' owned by: " + ChatColor.WHITE + playerData.get("name"));
            return ReturnCode.SUCCESS;
        } else {
            return ReturnCode.WARP_NOT_FOUND;
        }
    }

    public void tryCreateTable() throws SQLException {
        executeUpdate(
                "CREATE TABLE IF NOT EXISTS warps (" +
                "id INT PRIMARY KEY AUTO_INCREMENT, " +
                "name VARCHAR(300), " +
                "x DOUBLE," +
                "y DOUBLE," +
                "z DOUBLE," +
                "yaw FLOAT," +
                "pitch FLOAT," +
                "world VARCHAR(300)," +
                "uuid VARCHAR(36))"
        );
    }
}
