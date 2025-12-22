package org.miraimc.teamPvP.world;

import org.bukkit.*;
import org.miraimc.teamPvP.TeamPvP;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class WorldManager {

    private final TeamPvP plugin;

    public WorldManager(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void loadWorlds() {
        loadWorldFromDataFolder("lobby");
        loadWorldFromDataFolder("death_match");
    }

    private void loadWorldFromDataFolder(String folderName) {
        File source = new File(plugin.getDataFolder(), "worlds/" + folderName);
        File target = new File(Bukkit.getWorldContainer(), folderName);

        if (!source.exists()) {
            plugin.getLogger().warning("World directory does not exist: " + source.getPath());
            generateVoidWorld(folderName);
            return;
        }

        try {
            // 기존 월드 삭제
            if (target.exists()) {
                plugin.getLogger().info("Deleting world: " + target.getPath());
                org.apache.commons.io.FileUtils.deleteDirectory(target);
            }

            // 데이터폴더 → 서버 루트 복사
            plugin.getLogger().info("Copying world: " + source.getPath() + " → " + target.getPath());
            org.apache.commons.io.FileUtils.copyDirectory(source, target);

            // WorldCreator로 로드
            WorldCreator creator = new WorldCreator(folderName);
            creator.environment(World.Environment.NORMAL);
            creator.createWorld();

            plugin.getLogger().info("Loaded world: " + folderName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateVoidWorld(String worldName) {
        if (Bukkit.getWorld(worldName) == null) {
            WorldCreator worldCreator = new WorldCreator(worldName);
            worldCreator.type(WorldType.FLAT);
            worldCreator.generator(new VoidChunkGenerator());
            worldCreator.generateStructures(false);
            Bukkit.createWorld(worldCreator);
        }
    }

    public void setGameRules() {
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            world.setGameRule(GameRule.LOCATOR_BAR, false);
            world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);

            if (world.getName().equalsIgnoreCase("lobby") || world.getName().equalsIgnoreCase("death_match")) {
                world.setTime(6000L);
                world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
            }
        }
    }

    public void setBorder() {
        for (World world : Bukkit.getWorlds()) {
            String worldName = world.getName();
            if (worldName.equalsIgnoreCase("world") ||
                    worldName.equalsIgnoreCase("world_nether") ||
                    worldName.equalsIgnoreCase("world_the_end")) {
                WorldBorder border = world.getWorldBorder();
                border.setCenter(0, 0);
                border.setSize(plugin.getGameSettings().getDefaultBorderSize());
            }
        }
    }
}
