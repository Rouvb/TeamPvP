package org.miraimc.teamPvP.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameSettings;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.event.ProfileAddEvent;
import org.miraimc.teamPvP.profile.event.ProfileStatusEvent;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.team.event.TeamAddMemberEvent;
import org.miraimc.teamPvP.team.event.TeamMergeEvent;
import org.miraimc.teamPvP.team.event.TeamRemoveMemberEvent;

import java.util.HashSet;
import java.util.Set;

public class GameListener implements Listener {

    private final TeamPvP plugin;
    private final Set<Location> placedBlocks = new HashSet<>();

    public GameListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onProfileAdd(ProfileAddEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getTeam(player);

        if (team == null) {
            GameStatus status = plugin.getGame().getGameStatus();
            if (status == GameStatus.PLAYING || status == GameStatus.DEATH_MATCH) {
                plugin.getSpectatorHandler().handleSpectator(player);
                teleportToHighestBlock(player, plugin.getGame().getGameLocation());
            }
        }
    }

    /**
     * 지정된 위치의 가장 높은 블록 위로 플레이어를 텔레포트
     */
    private void teleportToHighestBlock(Player player, Location baseLocation) {
        if (baseLocation == null || baseLocation.getWorld() == null) return;

        Location highestLocation = baseLocation.getWorld()
                .getHighestBlockAt(baseLocation)
                .getLocation()
                .add(0, 1, 0);

        player.teleport(highestLocation);
    }

    /**
     * 지정된 위치로 플레이어를 텔레포트
     */
    private void teleportToLocation(Player player, Location location) {
        if (location == null || location.getWorld() == null) return;
        player.teleport(location);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = event.getEntity().getKiller();
        plugin.getGameHandler().handleDeath(victim, killer);
    }

    private void checkEndGame() {
        Team winnerTeam = plugin.getTeamManager().getWinnerTeam();
        if (winnerTeam != null) {
            plugin.getGameManager().endGame(winnerTeam);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getTeam(player);

        // 팀이 없으면 게임 월드의 최고 지점으로
        if (team == null || team.getLeader() == null) {
            Location gameLocation = plugin.getGame().getGameLocation();
            if (gameLocation != null && gameLocation.getWorld() != null) {
                Location highestLocation = gameLocation.getWorld()
                        .getHighestBlockAt(gameLocation)
                        .getLocation()
                        .add(0, 1, 0);
                event.setRespawnLocation(highestLocation);
            }
            return;
        }

        Game game = plugin.getGame();
        GameStatus status = game.getGameStatus();

        // 데스매치 상태면 데스매치 위치로
        if (status == GameStatus.DEATH_MATCH) {
            Location deathMatchLoc = player.getLocation().add(0, 3, 0);
            event.setRespawnLocation(deathMatchLoc);
            return;
        }

        // 게임 중이면 리더 위치로
        if (status == GameStatus.PLAYING) {
            Player leader = Bukkit.getPlayer(team.getLeader());
            if (leader != null && leader.isOnline()) {
                event.setRespawnLocation(leader.getLocation());
            } else {
                // 리더가 없으면 게임 위치로
                Location gameLocation = plugin.getGame().getGameLocation();
                if (gameLocation != null) {
                    event.setRespawnLocation(gameLocation);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberDeath(PlayerDeathEvent event) {
        checkEndGame();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMerge(TeamMergeEvent event) {
        checkEndGame();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberRemove(TeamRemoveMemberEvent event) {
        checkEndGame();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberAdd(TeamAddMemberEvent event) {
        checkEndGame();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeamMemberRespawn(PlayerRespawnEvent event) {
        checkEndGame();
    }

    @EventHandler
    public void onProfileChangeStatus(ProfileStatusEvent event) {
        checkEndGame();
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager) || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        Game game = plugin.getGame();
        GameSettings gameSettings = plugin.getGameSettings();
        if (game.getGameTime() <= gameSettings.getInvincibilityTime()) {
            event.setCancelled(true);
            return;
        }

        Team victimTeam = plugin.getTeamManager().getTeam(victim);
        Team damagerTeam = plugin.getTeamManager().getTeam(damager);

        if (victimTeam != null && damagerTeam != null && victimTeam.equals(damagerTeam)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        Game game = plugin.getGame();
        GameSettings gameSettings = plugin.getGameSettings();
        if (game.getGameTime() <= gameSettings.getInvincibilityTime()) {
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
