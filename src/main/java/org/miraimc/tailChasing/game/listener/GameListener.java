package org.miraimc.tailChasing.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.team.Team;

public class GameListener implements Listener {

    private final TailChasing plugin;

    public GameListener(TailChasing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();

        if (killer != null) {
            Team killerTeam = plugin.getTeamManager().getTeam(killer);
            plugin.getTeamHandler().handleTeam(killerTeam, victim);
        }
    }
}
