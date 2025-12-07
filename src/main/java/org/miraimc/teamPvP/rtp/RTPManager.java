package org.miraimc.teamPvP.rtp;

import lombok.Getter;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.miraimc.teamPvP.TeamPvP;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class RTPManager {

    private final TeamPvP plugin;
    @Getter private final List<Location> locations = Collections.synchronizedList(new ArrayList<>());
    @Getter private final RTPTask rtpTask;

    public RTPManager(TeamPvP plugin) {
        this.plugin = plugin;
        this.rtpTask = new RTPTask(plugin);
        rtpTask.runTaskTimerAsynchronously(plugin, 20L, 20L * 60 * 5);
    }

    /**
     * -range ~ +range 범위에서 랜덤 좌표 생성
     * 예: range=500 -> -500 ~ 500
     */
    private int getRandomCoordinate(int range) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        return random.nextInt(-range, range + 1);
    }

    private boolean isUnsafeMaterial(Material material) {
        if (material == null) return true;
        if (material.isAir()) return true;
        return switch (material) {
            case WATER, LAVA, KELP, SEAGRASS, TALL_SEAGRASS, FIRE, SOUL_FIRE, MAGMA_BLOCK -> true;
            default -> false;
        };
    }

    /**
     * Find a safe location asynchronously using Purpur/Paper async Chunk API.
     * This method avoids calling main-thread-only Bukkit APIs directly except for
     * the initial async chunk request (provided by the server implementation).
     */
    public CompletableFuture<Location> findSafeLocationAsync(World world, int range, int maxTries) {
        CompletableFuture<Location> promise = new CompletableFuture<>();

        if (world == null) {
            promise.completeExceptionally(new IllegalArgumentException("world is null"));
            return promise;
        }
        if (maxTries <= 0) {
            promise.complete(null);
            return promise;
        }

        attemptFind(world, range, maxTries, 0, promise);
        return promise;
    }

    private void attemptFind(World world, int range, int maxTries, int attemptIndex, CompletableFuture<Location> promise) {
        if (promise.isDone()) return;

        if (attemptIndex >= maxTries) {
            promise.complete(null);
            return;
        }

        // 더 분산된 좌표를 위해 개선된 랜덤 생성
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 시도 횟수에 따라 다른 범위 사용 (초반에는 넓게, 나중에는 전체 범위)
        int effectiveRange = (attemptIndex < 3)
                ? range  // 처음 3번은 전체 범위
                : (int)(range * (0.7 + random.nextDouble() * 0.3)); // 이후는 70-100% 범위

        // 좌표 생성
        int tempX = random.nextInt(-effectiveRange, effectiveRange + 1);
        int tempZ = random.nextInt(-effectiveRange, effectiveRange + 1);

        // 중심부(0,0) 근처 회피 (반경 50 이내는 80% 확률로 외곽으로 이동)
        if (Math.abs(tempX) < 50 && Math.abs(tempZ) < 50 && random.nextDouble() < 0.8) {
            // 외곽으로 밀어내기
            int direction = random.nextInt(4);
            switch (direction) {
                case 0 -> tempX = random.nextInt(range / 2, range + 1);          // 동쪽
                case 1 -> tempX = random.nextInt(-range, -(range / 2) + 1);      // 서쪽
                case 2 -> tempZ = random.nextInt(range / 2, range + 1);          // 남쪽
                case 3 -> tempZ = random.nextInt(-range, -(range / 2) + 1);      // 북쪽
            }
        }

        final int x = tempX;
        final int z = tempZ;
        Location probe = new Location(world, x, 0, z);

        try {
            world.getChunkAtAsync(probe, false)
                    .thenApplyAsync(chunk -> {
                        ChunkSnapshot snapshot = chunk.getChunkSnapshot(true, true, false);
                        return new ChunkWithSnapshot(chunk, snapshot);
                    }).thenAcceptAsync(cws -> {
                        if (promise.isDone()) return;

                        try {
                            int localX = Math.floorMod(x, 16);
                            int localZ = Math.floorMod(z, 16);
                            int highestY = cws.snapshot.getHighestBlockYAt(localX, localZ);

                            if (highestY <= 0) {
                                attemptFind(world, range, maxTries, attemptIndex + 1, promise);
                                return;
                            }

                            int groundY = highestY;
                            Material ground = cws.snapshot.getBlockType(localX, groundY, localZ);
                            Material above = cws.snapshot.getBlockType(localX, groundY + 1, localZ);
                            Material above2 = cws.snapshot.getBlockType(localX, groundY + 2, localZ);

                            boolean safe = true;
                            if (isUnsafeMaterial(ground)) safe = false;
                            if (above != null && !above.isAir()) safe = false;
                            if (above2 != null && !above2.isAir()) safe = false;

                            if (safe) {
                                Location found = new Location(world, x + 0.5, groundY + 1.0, z + 0.5);
                                promise.complete(found);
                            } else {
                                attemptFind(world, range, maxTries, attemptIndex + 1, promise);
                            }
                        } catch (Throwable t) {
                            plugin.getLogger().log(Level.FINE, "RTP attempt check failed on async snapshot", t);
                            attemptFind(world, range, maxTries, attemptIndex + 1, promise);
                        }
                    })
                    .exceptionally(ex -> {
                        plugin.getLogger().log(Level.FINER, "Failed to load chunk async for RTP (x=" + x + ", z=" + z + ")", ex);
                        attemptFind(world, range, maxTries, attemptIndex + 1, promise);
                        return null;
                    });
        } catch (NoSuchMethodError e) {
            plugin.getLogger().severe("Server does not support async chunk loading (getChunkAtAsync). RTP will fallback to safe sync behaviour.");
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                Location found = getSafeRandomLocationSync(world, range, maxTries);
                promise.complete(found);
            });
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "Unexpected error in attemptFind", t);
            attemptFind(world, range, maxTries, attemptIndex + 1, promise);
        }
    }

    @Nullable
    private Location getSafeRandomLocationSync(World world, int range, int maxTries) {
        for (int i = 0; i < maxTries; i++) {
            int x = getRandomCoordinate(range);
            int z = getRandomCoordinate(range);
            int y = world.getHighestBlockYAt(x, z);
            if (y <= 0) continue;
            Location location = new Location(world, x + 0.5, y + 1.0, z + 0.5);
            Material ground = location.getBlock().getType();
            Material above = location.clone().add(0, 1, 0).getBlock().getType();
            Material above2 = location.clone().add(0, 2, 0).getBlock().getType();

            if (!isUnsafeMaterial(ground) && above.isAir() && above2.isAir()) {
                return location;
            }
        }
        return null;
    }

    public void randomTeleportAsync(Player player, int range, Runnable onSuccess, Runnable onFail) {
        Location rtpLocation = plugin.getGame().getGameLocation();
        if (rtpLocation == null) {
            if (onFail != null) onFail.run();
            return;
        }

        World world = rtpLocation.getWorld();
        if (world == null) {
            if (onFail != null) onFail.run();
            return;
        }

        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");

        findSafeLocationAsync(world, range, maxTries).thenAccept(location -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (location != null && player.isOnline()) {
                    player.teleport(location);
                    if (onSuccess != null) onSuccess.run();
                } else {
                    if (onFail != null) onFail.run();
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Unexpected error finding safe RTP location: " + ex);
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (onFail != null) onFail.run();
            });
            return null;
        });
    }

    /**
     * Async Random Teleport
     */
    public void randomTeleportAsync(Player player, Runnable onSuccess, Runnable onFail) {
        int range = plugin.getConfigFile().getInt("rtp.range");
        randomTeleportAsync(player, range, onSuccess, onFail);
    }

    private static final class ChunkWithSnapshot {
        final Chunk chunk;
        final ChunkSnapshot snapshot;

        private ChunkWithSnapshot(Chunk chunk, ChunkSnapshot snapshot) {
            this.chunk = Objects.requireNonNull(chunk);
            this.snapshot = Objects.requireNonNull(snapshot);
        }
    }
}