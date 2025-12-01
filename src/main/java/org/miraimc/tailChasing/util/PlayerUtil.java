package org.miraimc.tailChasing.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.miraimc.tailChasing.TailChasing;

import java.util.UUID;

@UtilityClass
public class PlayerUtil {

    private static final BasicConfigFile config = TailChasing.getInstance().getConfigFile();

    public static void teleportToLobby(Player player) {
        String serializedLobbyLocation = config.getString("lobby.location");

        if (serializedLobbyLocation == null) {
            TailChasing.getInstance().getLogger().warning("Could not find lobby location in config");
            return;
        }

        Location lobbyLocation = LocationUtil.deserializeLocation(serializedLobbyLocation);
        if (lobbyLocation == null) {
            World world = player.getWorld();
            lobbyLocation = world.getSpawnLocation();
        }
        player.teleport(lobbyLocation);
    }

    public static void resetInventory(Player player, boolean leader) {
        if (leader) {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);
        } else {
            player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
        }
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
        player.setFoodLevel(20);
        player.setSaturation(20.0f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
    }

    public static String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return (player != null) ? player.getName() : "Unknown Player";
    }

}
