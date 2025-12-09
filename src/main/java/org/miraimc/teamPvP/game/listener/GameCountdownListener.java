package org.miraimc.teamPvP.game.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;

public class GameCountdownListener implements Listener {

    private final TeamPvP plugin;

    public GameCountdownListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    private boolean isInCountdown(Player player) {
        Profile profile = plugin.getProfileManager().getProfile(player);
        return profile != null && profile.getStatus() == ProfileStatus.STARTING;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (isInCountdown(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (isInCountdown(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!isInCountdown(player)) return;

        if (event.getFrom().getX() != event.getTo().getX() ||
                event.getFrom().getY() != event.getTo().getY() ||
                event.getFrom().getZ() != event.getTo().getZ()) {

            event.setTo(event.getFrom().setDirection(event.getTo().getDirection()));
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        if (isInCountdown(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player attacker) {
            if (isInCountdown(attacker)) {
                event.setCancelled(true);
            }
        }
    }
}
