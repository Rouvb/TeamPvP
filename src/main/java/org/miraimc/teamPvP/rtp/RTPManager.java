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
        // 더 넓은 분포를 위해 난수 생성 개선
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

        // 각 시도마다 완전히 새로운 랜덤 좌표 생성
        // ThreadLocalRandom을 명시적으로 호출하여 독립적인 난수 보장
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int x = random.nextInt(-range, range + 1);
        int z = random.nextInt(-range, range + 1);

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