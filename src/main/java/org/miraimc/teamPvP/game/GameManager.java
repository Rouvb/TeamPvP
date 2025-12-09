package org.miraimc.teamPvP.game;

import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.event.GameCountdownEvent;
import org.miraimc.teamPvP.game.event.GameDeathMatchEvent;
import org.miraimc.teamPvP.game.event.GameEndEvent;
import org.miraimc.teamPvP.game.event.GameStartEvent;
import org.miraimc.teamPvP.game.task.CountdownTask;
import org.miraimc.teamPvP.game.task.GameTask;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;

import javax.annotation.Nullable;
import java.util.Collection;

public class GameManager {

    private final TeamPvP plugin;

    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    public GameManager(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void startCountdown() {
        Game game = plugin.getGame();
        if (game.getGameStatus() != GameStatus.LOBBY) return;

        game.setGameStatus(GameStatus.STARTING);
        Bukkit.getPluginManager().callEvent(new GameCountdownEvent(plugin.getGame()));

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            plugin.getGameHandler().handleCountdownPlayer(player);
        }

        plugin.getGameHandler().teleportAllToGameWorld()
                .thenRun(() -> Bukkit.getScheduler().runTask(plugin, () -> {
                    cancelCountdown();
                    countdownTask = new CountdownTask(this, plugin.getGame())
                            .runTaskTimer(plugin, 0L, 20L);
                    plugin.getLogger().info("All players teleported! Starting countdown...");
                }))
                .exceptionally(ex -> {
                    plugin.getLogger().severe("Teleport failed: " + ex.getMessage());
                    return null;
                });
    }

    public void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
    }

    public void startGame() {
        Game game = plugin.getGame();
        if (game.getGameStatus() != GameStatus.STARTING) return;

        game.setGameStatus(GameStatus.PLAYING);
        cancelCountdown();

        Bukkit.getPluginManager().callEvent(new GameStartEvent(game));

        Team winnerTeam = plugin.getTeamManager().getWinnerTeam();
        if (winnerTeam != null) {
            endGame(winnerTeam);
            return;
        }

        Collection<? extends Player> onlinePlayers = Bukkit.getOnlinePlayers();
        for (Player player : onlinePlayers) {
            plugin.getGameHandler().handleStartPlayer(player);
        }

        cancelGameTask();
        gameTask = new GameTask(plugin.getGame(), this)
                .runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelGameTask() {
        if (gameTask != null) {
            gameTask.cancel();
            gameTask = null;
        }
    }

    public void startDeathMatch() {
        Game game = plugin.getGame();
        if (game.getGameStatus() != GameStatus.PLAYING) return;
        game.setGameStatus(GameStatus.DEATH_MATCH);
        Bukkit.getPluginManager().callEvent(new GameDeathMatchEvent(game));
        plugin.getGameHandler().sendDeathMatchAlert();
        plugin.getGameHandler().teleportAllToDeathMatch();
    }

    public void endGame(@Nullable Team winnerTeam) {
        Game game = plugin.getGame();
        GameStatus gameStatus = plugin.getGame().getGameStatus();
        if (gameStatus != GameStatus.PLAYING && gameStatus != GameStatus.DEATH_MATCH) return;

        game.setGameStatus(GameStatus.ENDING);
        plugin.getGameHandler().handleEndGame(winnerTeam);

        Bukkit.getPluginManager().callEvent(new GameEndEvent(game));

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "stop");
        }, 20L * 15);
    }

    /**
     * Game 클래스에 위치 정보를 저장하는 메서드
     * @param locationType
     */
    public void setLocation(String locationType) {
        Location location = LocationUtil.deserializeLocation(
                plugin.getConfigFile().getString("game.locations." + locationType)
        );
        if (location == null) return;

        switch (locationType) {
            case "lobby" -> plugin.getGame().setLobbyLocation(location);
            case "game" -> plugin.getGame().setGameLocation(location);
            case "death_match" -> plugin.getGame().setDeathMatchLocation(location);
            default -> plugin.getLogger().warning("Unknown location type: " + locationType);
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
