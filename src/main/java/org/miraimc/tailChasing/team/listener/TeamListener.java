package org.miraimc.tailChasing.team.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.team.Team;

public class TeamListener implements Listener {

    private final TailChasing plugin;

    public TeamListener(TailChasing plugin) {
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
