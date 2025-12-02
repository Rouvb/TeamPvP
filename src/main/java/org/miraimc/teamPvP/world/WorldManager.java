package org.miraimc.teamPvP.world;

import org.bukkit.*;
import org.miraimc.teamPvP.TeamPvP;

import java.util.List;

public class WorldManager {

    private final TeamPvP plugin;

    public WorldManager(TeamPvP plugin) {
        this.plugin = plugin;
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

    public void generateWorlds() {
        generateVoidWorld("lobby");
        generateVoidWorld("death_match");
    }

    public void setGameRules() {
        List<World> worlds = Bukkit.getWorlds();
        for (World world : worlds) {
            world.setGameRule(GameRule.LOCATOR_BAR, false);

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
                border.setSize(plugin.getGame().getBorderSize());
            }
        }
    }
}
