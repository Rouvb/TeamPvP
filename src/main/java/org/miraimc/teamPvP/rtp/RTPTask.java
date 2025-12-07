package org.miraimc.teamPvP.rtp;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.util.LocationUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

        // 병렬로 여러 좌표를 동시에 생성하여 더 다양한 랜덤 값 확보
        generateLocationsInParallel(world, range, maxTries, targetCount, successCount, failCount);
    }

    private void generateLocationsInParallel(World world, int range, int maxTries, int targetCount,
                                             AtomicInteger successCount, AtomicInteger failCount) {
        List<CompletableFuture<Location>> futures = new ArrayList<>();

        // 동시에 여러 좌표 생성 요청 (배치 크기: 20개씩)
        int batchSize = 20;
        for (int i = 0; i < targetCount; i += batchSize) {
            int remainingCount = Math.min(batchSize, targetCount - i);

            for (int j = 0; j < remainingCount; j++) {
                CompletableFuture<Location> future = plugin.getRtpManager()
                        .findSafeLocationAsync(world, range, maxTries)
                        .thenApply(location -> {
                            if (location != null) {
                                successCount.incrementAndGet();
                                return location;
                            } else {
                                failCount.incrementAndGet();
                                return null;
                            }
                        })
                        .exceptionally(ex -> {
                            plugin.getLogger().warning("Error generating RTP location: " + ex.getMessage());
                            failCount.incrementAndGet();
                            return null;
                        });

                futures.add(future);
            }

            // 배치가 완료될 때까지 약간 대기 (다음 배치는 다른 시드 사용)
            int currentBatch = i / batchSize;
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                    .thenRun(() -> {
                        // 성공한 좌표들만 추가
                        futures.forEach(f -> f.thenAccept(loc -> {
                            if (loc != null) {
                                plugin.getRtpManager().getLocations().add(loc);
                            }
                        }));

                        // 마지막 배치 완료 후 로그 출력
                        if ((currentBatch + 1) * batchSize >= targetCount) {
                            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                                plugin.getLogger().info("RTP location generation completed: success=" +
                                        successCount.get() + ", failed=" + failCount.get() +
                                        ", total=" + plugin.getRtpManager().getLocations().size());
                            }, 20L);
                        }
                    });

            // 다음 배치 전 짧은 지연 (더 다양한 시드)
            if (i + batchSize < targetCount) {
                try {
                    Thread.sleep(100); // 100ms 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }

            futures.clear();
        }
    }
}