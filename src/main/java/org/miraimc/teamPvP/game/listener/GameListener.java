package org.miraimc.teamPvP.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
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

        // Instant Respawn
        Bukkit.getScheduler().runTaskLater(plugin, () -> victim.spigot().respawn(), 2L);

        Team team = plugin.getTeamManager().getTeam(victim);
        if (team == null) return;

        Game game = plugin.getGame();
        if (game.getGameStatus().equals(GameStatus.DEATH_MATCH)) {
            plugin.getGameManager().setSpectatorMode(victim);
            return;
        }

        Player killer = event.getEntity().getKiller();
        if (killer != null
                && killer != victim
                && !team.getMembers().contains(killer.getUniqueId())) {

            Team killerTeam = plugin.getTeamManager().getTeam(killer);
            if (killerTeam == null) return;

            // 리더 사망
            if (team.getLeader() != null && team.getLeader().equals(victim.getUniqueId())) {
                for (UUID uuid : team.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && member.isOnline()) {
                        plugin.getTeamHandler().handleTeam(killerTeam, member);
                        setReducedHealth(member);
                    }
                }
            } else {
                plugin.getTeamHandler().handleTeam(killerTeam, victim);
                setReducedHealth(victim);
            }
            return;
        }

        UUID leaderId = team.getLeader();
        if (leaderId != null) {
            Player leader = Bukkit.getPlayer(leaderId);
            if (leader != null && leader.isOnline()) {
                victim.teleport(leader.getLocation());
            }
        }
    }

    private void setReducedHealth(Player player) {
        AttributeInstance maxHealthAttr = player.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(5.0);
            player.setHealth(5.0);
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
