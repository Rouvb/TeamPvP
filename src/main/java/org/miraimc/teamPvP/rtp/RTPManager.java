package org.miraimc.teamPvP.rtp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class RTPManager {

    public static void teleportPlayer(Player player, String worldName, int range) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("World " + worldName + " not found");
            return;
        }

        SafeLocationFinder.findSafeLocationAsync(world, range, loc -> {
            if (loc == null) {
                Bukkit.getLogger().warning("Failed to teleport to world " + worldName);
            } else {
                player.teleportAsync(loc);
            }
        });
    }
}