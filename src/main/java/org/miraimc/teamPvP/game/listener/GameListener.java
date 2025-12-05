package org.miraimc.teamPvP.game.listener;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.team.event.TeamRemoveMemberEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GameListener implements Listener {

    private final TeamPvP plugin;
    private final Set<Location> placedBlocks = new HashSet<>();

    public GameListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();

        Team team = plugin.getTeamManager().getTeam(victim);
        if (team == null) return;

        Game game = plugin.getGame();
        if (game.getGameStatus().equals(GameStatus.DEATH_MATCH)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> victim.spigot().respawn(), 4L);
            Title title = Title.title(
                    Component.text(Lang.GAME_DEATH_SPECTATOR_TITLE.getMessage()),
                    Component.text(Lang.GAME_DEATH_SPECTATOR_SUBTITLE.getMessage())
            );
            victim.showTitle(title);
            plugin.getGameManager().setSpectatorMode(victim);
            return;
        }

        Title title = Title.title(
                Component.text(Lang.GAME_DEATH_PLAYER_TITLE.getMessage()),
                Component.text(Lang.GAME_DEATH_PLAYER_SUBTITLE.getMessage())
        );
        victim.showTitle(title);

        Player killer = event.getEntity().getKiller();
        if (killer != null
                && killer != victim
                && !team.getMembers().contains(killer.getUniqueId())) {

            Team killerTeam = plugin.getTeamManager().getTeam(killer);
            if (killerTeam == null) return;

            plugin.getTeamHandler().handleTeam(killerTeam, victim);
            setReducedHealth(victim);
        }

        UUID leaderId = team.getLeader();
        if (leaderId != null) {
            Player leader = Bukkit.getPlayer(leaderId);
            if (leader != null && leader.isOnline()) {
                victim.teleport(leader.getLocation());
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Team team = plugin.getTeamManager().getTeam(event.getPlayer());
        if (team == null) return;
        Player leader = Bukkit.getPlayer(team.getLeader());
        if (leader != null && leader.isOnline()) {
            event.setRespawnLocation(leader.getLocation());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberDeath(PlayerDeathEvent event) {
        Game game = plugin.getGame();
        GameStatus gameStatus = game.getGameStatus();
        if (!(gameStatus.equals(GameStatus.PLAYING) || gameStatus.equals(GameStatus.DEATH_MATCH))) return;

        Team winnerTeam = plugin.getTeamManager().getWinnerTeam();
        if (winnerTeam == null) return;
        plugin.getGameManager().endGame(winnerTeam);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberLeave(TeamRemoveMemberEvent event) {
        Game game = plugin.getGame();
        GameStatus gameStatus = game.getGameStatus();
        if (!(gameStatus.equals(GameStatus.PLAYING) || gameStatus.equals(GameStatus.DEATH_MATCH))) return;

        Team winnerTeam = plugin.getTeamManager().getWinnerTeam();
        if (winnerTeam == null) return;
        plugin.getGameManager().endGame(winnerTeam);
    }

    private void setReducedHealth(Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(10.0);
            player.setHealth(10.0);
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
        if (game.getDuration() <= game.getInvincibilityTime()) {
            event.setCancelled(true);
        }

        if (victimTeam != null && victimTeam.equals(damagerTeam)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Game game = plugin.getGame();
        if (game.getDuration() <= game.getInvincibilityTime()) {
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
