package org.miraimc.teamPvP.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.HashSet;
import java.util.Set;

public class GameListener implements Listener {

    private final TeamPvP plugin;
    private final Set<Location> placedBlocks = new HashSet<>();

    public GameListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Bukkit.getScheduler().runTaskLater(plugin, () -> player.spigot().respawn(), 2L);

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) return;

        Game game = plugin.getGame();
        if (game.getGameStatus().equals(GameStatus.DEATH_MATCH)) {
            plugin.getGameManager().setSpectatorMode(player);
            return;
        }

        Player leader = Bukkit.getPlayer(team.getLeader());
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

        Game game = plugin.getGame();
        if (game.getDuration() <= 300) {
            damager.sendMessage(TextUtil.translate("&c무적 시간에는 상대방을 공격할 수 없습니다."));
            event.setCancelled(true);
        }

        if (victimTeam != null && damagerTeam != null && victimTeam.equals(damagerTeam)) {
            damager.sendMessage(TextUtil.translate("&c같은 팀원을 공격할 수 없습니다."));
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Game game = plugin.getGame();
        if (game.getDuration() <= 300) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (event.getBlock().getWorld().getName().equalsIgnoreCase("death_match")) {
            placedBlocks.add(event.getBlock().getLocation());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getWorld().getName().equalsIgnoreCase("death_match")) {
            Location location = event.getBlock().getLocation();
            if (!placedBlocks.contains(location)) {
                event.setCancelled(true);
            } else {
                placedBlocks.remove(location);
            }
        }
    }
}
