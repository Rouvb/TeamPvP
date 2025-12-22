package org.miraimc.teamPvP.rtp;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.event.TeamAddMemberEvent;
import org.miraimc.teamPvP.team.event.TeamMergeEvent;
import org.miraimc.teamPvP.team.event.TeamRemoveMemberEvent;

public class RTPListener implements Listener {

    private final TeamPvP plugin;

    public RTPListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    private void addCacheLocation() {
        String worldName = "world";
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            Bukkit.getLogger().warning("World " + worldName + " not found");
            return;
        }
        int range = plugin.getGameSettings().getRtpRange();

        SafeLocationFinder.findSafeLocationAsync(world, range, loc -> {
            if (loc == null) {
                Bukkit.getLogger().warning("Failed to add location to RTPManager Cache " + worldName);
            } else {
                RTPManager.getLocations().add(loc);
            }
        });
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        addCacheLocation();
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        addCacheLocation();
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        addCacheLocation();
    }

    @EventHandler
    public void onTeamAddMember(TeamAddMemberEvent event) {
        addCacheLocation();
    }

    @EventHandler
    public void onTeamRemoveMember(TeamRemoveMemberEvent event) {
        addCacheLocation();
    }

    @EventHandler
    public void onTeamMerge(TeamMergeEvent event) {
        addCacheLocation();
    }
}
