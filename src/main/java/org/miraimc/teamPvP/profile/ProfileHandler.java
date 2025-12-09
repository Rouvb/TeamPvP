package org.miraimc.teamPvP.profile;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;

import java.util.UUID;

public class ProfileHandler {

    private final TeamPvP plugin;

    public ProfileHandler(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void resetInventory(Player player) {
        ProfileStatus profileStatus = plugin.getProfileManager().getProfile(player).getStatus();
        Team team = plugin.getTeamManager().getTeam(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getInventory().setItemInOffHand(null);
        for (PotionEffect effect : player.getActivePotionEffects()) {
            player.removePotionEffect(effect.getType());
        }
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
                player.setAllowFlight(true);
                player.setFlying(true);
                player.getAttribute(Attribute.MAX_HEALTH).setBaseValue(20.0);
            }
        }
        player.setHealth(player.getAttribute(Attribute.MAX_HEALTH).getBaseValue());
    }

    public String getPlayerName(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return (player != null) ? player.getName() : "Unknown Player";
    }
}
