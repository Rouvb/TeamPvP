package org.miraimc.teamPvP.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

public class GameListener implements Listener {

    private final TeamPvP plugin;

    public GameListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Team team = plugin.getTeamManager().getTeam(player);
        Player leader = Bukkit.getPlayer(team.getLeader());
        Game game = plugin.getGame();

        if (game.getGameStatus().equals(GameStatus.DEATH_MATCH)) {
            plugin.getGameManager().setSpectatorMode(player);
            return;
        }

        if (leader != null) {
            player.teleport(leader.getLocation());
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player damager = (Player) event.getDamager();

        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        Team damagerTeam = plugin.getTeamManager().getTeam(damager);

        if (victimTeam != null && damagerTeam != null && victimTeam.equals(damagerTeam)) {
            damager.sendMessage(TextUtil.translate("&c같은 팀원을 공격할 수 없습니다."));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInvincibleTimer(EntityDamageEvent event) {
        Game game = plugin.getGame();
        if (game.getDuration() <= 300) {
            event.setCancelled(true);
        }
    }
}
