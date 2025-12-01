package org.miraimc.tailChasing.profile;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ProfileManager {

    private final List<Profile> profiles = new ArrayList<>();

    public void addProfile(Player player) {
        Profile profile = new Profile(player.getUniqueId(), ProfileStatus.LOBBY);
        profiles.add(profile);
    }

    public Profile getProfile(Player player) {
        return profiles.stream().filter(profile -> profile.getUuid() == player.getUniqueId()).findFirst().orElse(null);
    }

    public void removeProfile(Player player) {
        Profile profile = getProfile(player);
        if (profile != null) {
            profiles.remove(profile);
        }
    }
}
