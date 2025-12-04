package org.miraimc.teamPvP.profile.listener;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.miraimc.teamPvP.TeamPvP;

public class WorldPreventionListener implements Listener {

    private final TeamPvP plugin;

    public WorldPreventionListener(TeamPvP plugin) {
        this.plugin = plugin;
    }

    private boolean isPreventionWorld(World world) {
        World lobbyWorld = plugin.getGame().getLobbyLocation().getWorld();
        World deathMatchWorld = plugin.getGame().getDeathMatchLocation().getWorld();
        return world != null && (world.equals(lobbyWorld) || world.equals(deathMatchWorld));
    }

    @EventHandler
    public void onBurn(BlockBurnEvent event) {
        if (isPreventionWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        if (isPreventionWorld(event.getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        if (isPreventionWorld(event.getBlock().getWorld())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        if (isPreventionWorld(event.getLocation().getWorld())) {
            event.setCancelled(true);
        }
    }
}
