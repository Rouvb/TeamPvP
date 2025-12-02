package org.miraimc.teamPvP.util;

import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.profile.ProfileStatus;

@UtilityClass
public class VisibilityUtil {

    public static void updatePlayerVisibility(Player player, ProfileStatus profileStatus) {
        if (profileStatus.equals(ProfileStatus.PLAYING) || profileStatus.equals(ProfileStatus.WAITING)) {
            Bukkit.getOnlinePlayers().stream().filter(other -> other != player).forEach(other -> other.showPlayer(player));
        } else if (profileStatus.equals(ProfileStatus.SPECTATING)) {
            Bukkit.getOnlinePlayers().stream().filter(other -> other != player).forEach(other -> other.hidePlayer(player));
        }
    }
}
