package org.miraimc.teamPvP.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;

import java.util.UUID;

@UtilityClass
public class PlayerUtil {

    public static void resetInventory(Player player) {
        TeamPvP plugin = TeamPvP.getInstance();
        ProfileStatus profileStatus = plugin.getProfileManager().getProfile(player).getStatus();
        Team team = plugin.getTeamManager().getTeam(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        player.getActivePotionEffects().clear();
        player.setFoodLevel(20);
        player.setSaturation(5.0f);
        player.setExp(0.0f);
        player.setLevel(0);

        switch (profileStatus) {
            case ProfileStatus.WAITING -> {
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            }
            case ProfileStatus.PLAYING -> {
                player.setGameMode(GameMode.SURVIVAL);
                if (team.getLeader().equals(player.getUniqueId())) {
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(40.0);
                } else {
                    player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
                }
            }
            case ProfileStatus.SPECTATING -> {
                player.setGameMode(GameMode.CREATIVE);
                player.setAllowFlight(false);
                player.setFlying(false);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            }
        }
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
    }

    public static String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return (player != null) ? player.getName() : "Unknown Player";
    }
}