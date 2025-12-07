package org.miraimc.teamPvP.rtp;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RTPManager {

    @Getter private static final List<Location> locations = new ArrayList<>();

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