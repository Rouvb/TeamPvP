package org.miraimc.teamPvP.profile;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.profile.event.ProfileAddEvent;
import org.miraimc.teamPvP.profile.event.ProfileRemoveEvent;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private final List<Profile> profiles = new ArrayList<>();

    public Profile addProfile(Player player, ProfileStatus profileStatus) {
        Profile profile = new Profile(player.getUniqueId(), profileStatus);
        profiles.add(profile);
        Bukkit.getPluginManager().callEvent(new ProfileAddEvent(player, profile));
        return profile;
    }

    public Profile getProfile(Player player) {
        return profiles.stream().filter(profile -> profile.getUuid() == player.getUniqueId()).findFirst().orElse(null);
    }

    public void removeProfile(Player player) {
        Profile profile = getProfile(player);
        if (profile != null) {
            profiles.remove(profile);
            Bukkit.getPluginManager().callEvent(new ProfileRemoveEvent(player, profile));
        }
    }

    public void shutdown() {
        profiles.clear();
    }
}
