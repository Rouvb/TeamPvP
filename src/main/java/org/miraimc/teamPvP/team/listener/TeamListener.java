package org.miraimc.teamPvP.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;

public class TeamListener implements Listener {

    private final TeamPvP plugin;

    public TeamListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getTeam(player);

        if (team != null) {
            plugin.getTeamHandler().handleTeam(null, player);
        }
    }
}
