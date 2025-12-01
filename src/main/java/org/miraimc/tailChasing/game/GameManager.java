package org.miraimc.tailChasing.game;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.miraimc.tailChasing.TailChasing;
import org.miraimc.tailChasing.game.task.CountdownTask;
import org.miraimc.tailChasing.game.task.GameTask;
import org.miraimc.tailChasing.profile.ProfileStatus;
import org.miraimc.tailChasing.util.LocationUtil;

public class GameManager {

    private final TailChasing plugin;

    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    public GameManager(TailChasing plugin) {
        this.plugin = plugin;
    }

    public void startCountdown() {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) return;

        plugin.getGame().setGameStatus(GameStatus.STARTING);

        cancelCountdown();
        CountdownTask task = new CountdownTask(this, plugin.getGame());
        countdownTask = task.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public void startGame() {
        if (plugin.getGame().getGameStatus() != GameStatus.STARTING) return;

        plugin.getGame().setGameStatus(GameStatus.PLAYING);

        cancelCountdown();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                plugin.getGameHandler().preparePlayerForGame(player);
                plugin.getProfileManager().getProfile(player).setStatus(ProfileStatus.IN_GAME);
            });
        });

        // 무작위 위치로 텔레포트
        plugin.getTeamManager().getTeams().forEach(team -> plugin.getGameHandler().teleportTeamRandomWorld(team));

        cancelGameTask();
        GameTask task = new GameTask(plugin.getGame(), this);
        task.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelGameTask() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
    }

    public void startDeathMatch() {
        if (plugin.getGame().getGameStatus() != GameStatus.PLAYING) return;
        plugin.getGame().setGameStatus(GameStatus.DEATH_MATCH);
        plugin.getGameHandler().sendDeathMatchAlert();
        plugin.getGameHandler().teleportAllToDeathMatch();
    }

    public void endGame() {
        GameStatus gameStatus = plugin.getGame().getGameStatus();
        if (gameStatus != GameStatus.PLAYING && gameStatus != GameStatus.DEATH_MATCH) return;
        plugin.getGame().setGameStatus(GameStatus.ENDED);
    }

    public void loadLocation(String locationType) {
        Location location = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations." + locationType));
        if (location == null) return;
        switch (locationType) {
            case "lobby":
                plugin.getGame().setGameStatus(GameStatus.LOBBY);
                return;
            case "game":
                plugin.getGame().setLobbyLocation(location);
                return;
            default:
                plugin.getLogger().warning("Unknown location type: " + locationType);
        }
    }

    public void soundAll(Sound sound) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.playSound(player.getLocation(), sound, 1F, 1F);
        });
    }

    public void messageAll(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            player.sendMessage(message);
        });
    }

    public void TitleAll(Title title) {
        Bukkit.getOnlinePlayers().forEach(player -> {
           player.showTitle(title);
        });
    }
}
