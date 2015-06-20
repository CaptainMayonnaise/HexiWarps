package com.hexicraft.hexiwarps;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public enum ReturnCode {
    SUCCESS("", false),
    NOT_PLAYER("This command can only be run by a player.", false),
    INVALID_COMMAND("An invalid command was entered.", true),
    TOO_FEW_ARGUMENTS("Not enough arguments.", true),
    MYSQL_ERROR("A database error has occured, if the problem persists contact an Administrator.", false),
    WARP_NOT_FOUND("This warp doesn't exist.", false),
    MAX_WARPS("You have reached your maximum allowed warps.", false),
    NOT_OWNER("You do not own this warp!", false),
    NO_WARPS_CREATED("No warps have been created.", false),
    NO_WARPS_PLAYER("This player has no warps.", false),
    INVALID_SYNTAX("There was an error in the syntax of the arguments.", true),
    INVALID_ARGUMENT("The arguments entered were invalid.", true);

    private String message;
    private boolean sendUsage;

    ReturnCode(String message, boolean sendUsage) {
        this.message = message;
        this.sendUsage = sendUsage;
    }

    /**
     * Does the code have a message
     * @return true if has a message, false if empty
     */
    public boolean hasMessage() {
        return !(message.equals(""));
    }

    /**
     * Gets the return message, along with usage if required
     * @param cmd The command that was sent
     * @return The message
     */
    public String getMessage(Command cmd) {
        return message + (sendUsage ? ("\n" + ChatColor.GOLD + "Usage: " + ChatColor.RESET + cmd.getUsage()) : "");
    }
}
