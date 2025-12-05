package org.miraimc.teamPvP.rtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.util.LocationUtil;

public class RTPTask extends BukkitRunnable {

    private final TeamPvP plugin;

    public RTPTask(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Game game = plugin.getGame();
        if (game.getGameStatus().equals(GameStatus.DEATH_MATCH) || game.getGameStatus().equals(GameStatus.ENDING)) {
            cancel();
            return;
        }

        plugin.getRtpManager().getLocations().clear();
        Location worldLocation = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations.game"));
        World world = worldLocation.getWorld();
        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");
        int safeRange = plugin.getConfigFile().getInt("rtp.range");

        for (int i = 0; i < 100; i++) {
            plugin.getRtpManager().findSafeLocationAsync(world, safeRange, maxTries)
                    .thenAccept(location -> {
                        if (location == null) {
                            plugin.getLogger().warning("Can't find safe location");
                            return;
                        }
                        plugin.getRtpManager().getLocations().add(location);
                    });
        }
    }
}
