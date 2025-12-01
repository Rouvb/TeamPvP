package org.miraimc.tailChasing.rtp;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.util.LocationUtil;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

public class RTPManager {

    private final TailChasing plugin;

    public RTPManager(TailChasing plugin) {
        this.plugin = plugin;
    }

    private int getRandomRange(int range) {
        return ThreadLocalRandom.current().nextInt(-range, range + 1);
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

        int x = getRandomRange(range);
        int z = getRandomRange(range);

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
            int x = getRandomRange(range);
            int z = getRandomRange(range);
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
        if (rtpLocation == null) return;

        World world = rtpLocation.getWorld();
        if (world == null) {
            if (onFail != null) onFail.run();
            return;
        }

        int maxTries = plugin.getConfigFile().getInt("rtp.max_tries");
        int safeRange = Math.max(16, range);

        findSafeLocationAsync(world, safeRange, maxTries).thenAccept(location -> {
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                if (location != null && player.isOnline()) {
                    player.teleport(location);
                    if (onSuccess != null) onSuccess.run();
                } else {
                    if (onFail != null) onFail.run();
                }
            });
        }).exceptionally(ex -> {
            plugin.getLogger().warning("Unexpected error finding safe RTP Location" + ex);
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