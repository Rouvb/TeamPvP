package org.miraimc.teamPvP.rtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.util.LocationUtil;

import java.util.concurrent.atomic.AtomicInteger;

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

        if (worldLocation == null) {
            plugin.getLogger().warning("Game location is null, cannot generate RTP locations");
            return;
        }

        World world = worldLocation.getWorld();
        if (world == null) {
            plugin.getLogger().warning("Game world is null, cannot generate RTP locations");
            return;
        }

        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");
        int range = plugin.getConfigFile().getInt("rtp.range");
        int targetCount = 100;

        plugin.getLogger().info("Starting RTP location generation: target=" + targetCount + ", range=" + range);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // 순차적으로 비동기 작업 실행하여 더 다양한 랜덤 좌표 생성
        generateLocationsSequentially(world, range, maxTries, targetCount, 0, successCount, failCount);
    }

    private void generateLocationsSequentially(World world, int range, int maxTries, int targetCount,
                                               int currentIndex, AtomicInteger successCount, AtomicInteger failCount) {
        if (currentIndex >= targetCount) {
            plugin.getLogger().info("RTP location generation completed: success=" + successCount.get() +
                    ", failed=" + failCount.get() + ", total=" + plugin.getRtpManager().getLocations().size());
            return;
        }

        plugin.getRtpManager().findSafeLocationAsync(world, range, maxTries)
                .thenAccept(location -> {
                    if (location != null) {
                        plugin.getRtpManager().getLocations().add(location);
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                        plugin.getLogger().warning("Failed to find safe RTP location (attempt " + (currentIndex + 1) + ")");
                    }

                    // 다음 좌표 생성 (약간의 지연을 두어 더 다양한 랜덤 시드 사용)
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        generateLocationsSequentially(world, range, maxTries, targetCount,
                                currentIndex + 1, successCount, failCount);
                    }, 1L);
                })
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Error generating RTP location: " + ex.getMessage());
                    failCount.incrementAndGet();

                    // 에러가 발생해도 다음 좌표 생성 계속
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        generateLocationsSequentially(world, range, maxTries, targetCount,
                                currentIndex + 1, successCount, failCount);
                    }, 1L);
                    return null;
                });
    }
}