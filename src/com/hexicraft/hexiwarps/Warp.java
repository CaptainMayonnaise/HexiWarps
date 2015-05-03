package com.hexicraft.hexiwarps;

import org.bukkit.World;

import static org.bukkit.Bukkit.getServer;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public class Warp {
    private int id;
    private String name;
    private double x;
    private double y;
    private double z;
    private float yaw;
    private float pitch;
    private World world;
    private String uuid;

    Warp(int id, String name, double x, double y, double z, float yaw, float pitch, String world, String uuid) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.world = getServer().getWorld(world);
        this.uuid = uuid;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getYaw() {
        return yaw;
    }

    public float getPitch() {
        return pitch;
    }

    public World getWorld() {
        return world;
    }

    public String getUuid() {
        return uuid;
    }
}
