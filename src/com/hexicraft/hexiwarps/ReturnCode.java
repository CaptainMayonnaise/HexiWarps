package com.hexicraft.hexiwarps;

import org.bukkit.ChatColor;

/**
 * @author Ollie
 * @version %I%, %G%
 */
public enum ReturnCode {
    SUCCESS("", true, true),
    NOT_PLAYER(ChatColor.RED + "This command can only be run by a player.", false, true),
    INVALID_COMMAND(ChatColor.RED + "An invalid command was entered.", false, false),
    TOO_FEW_ARGUMENTS(ChatColor.GOLD + "Not enough arguments.", false, false),
    MYSQL_ERROR(ChatColor.RED + "A database error has occured, if the problem persists contact an Administrator.", false, true),
    WARP_NOT_FOUND(ChatColor.GOLD + "This warp doesn't exist.", false, true),
    MAX_WARPS(ChatColor.GOLD + "You have reached your maximum allowed warps.", false, true),
    NOT_OWNER(ChatColor.GOLD + "You do not own this warp!", false, true),
    NO_WARPS_CREATED(ChatColor.GOLD + "No warps have been created.", false, true),
    NO_WARPS_PLAYER(ChatColor.GOLD + "This player has no warps.", false, true),
    INVALID_SYNTAX(ChatColor.GOLD + "There was an error in the syntax of the arguments.", false, true);

    String message;
    boolean isSuccess;
    boolean hasValidSyntax;

    ReturnCode(String message, boolean isSuccess, boolean hasValidSyntax) {
        this.message = message;
        this.isSuccess = isSuccess;
        this.hasValidSyntax = hasValidSyntax;
    }
}
