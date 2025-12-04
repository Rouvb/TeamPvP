package org.miraimc.teamPvP.game;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.task.CountdownTask;
import org.miraimc.teamPvP.game.task.GameTask;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.TextUtil;
import org.miraimc.teamPvP.util.VisibilityUtil;

import javax.annotation.Nullable;

public class GameManager {

    private final TeamPvP plugin;

    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    public GameManager(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void startCountdown() {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) return;

        plugin.getGame().setGameStatus(GameStatus.STARTING);
        plugin.getGameHandler().getRandomTeleportLocations();

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
                plugin.getProfileManager().getProfile(player).setStatus(ProfileStatus.PLAYING);
                plugin.getGameHandler().preparePlayerForGame(player);
            });
        });

        plugin.getGameHandler().teleportAllToGameWorld();

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

    public void endGame(@Nullable Team winnerTeam) {
        GameStatus gameStatus = plugin.getGame().getGameStatus();
        if (gameStatus != GameStatus.PLAYING && gameStatus != GameStatus.DEATH_MATCH) return;
        plugin.getGame().setGameStatus(GameStatus.ENDING);
        plugin.getGameHandler().handleEndGame(winnerTeam);
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(plugin.getServer().getConsoleSender(), "stop");
        }, 20L * 15);
    }

    public void setSpectatorMode(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        if (team != null) {
            team.getAliveMembers().remove(player.getUniqueId());
        }

        Profile profile = plugin.getProfileManager().getProfile(player);
        profile.setStatus(ProfileStatus.SPECTATING);

        VisibilityUtil.updatePlayerVisibility(player, profile.getStatus());
        plugin.getProfileHandler().resetInventory(player);

        Title title = Title.title(
                Component.text(TextUtil.translate("&c&l사망")),
                Component.text(TextUtil.translate("&f이제 관전 모드 상태입니다!"))
        );
        player.showTitle(title);
    }

    /**
     * Game 클래스에 위치 정보를 저장하는 메서드
     * @param locationType
     */
    public void setLocation(String locationType) {
        Location location = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations." + locationType));
        if (location == null) return;
        switch (locationType) {
            case "lobby":
                plugin.getGame().setLobbyLocation(location);
                return;
            case "game":
                plugin.getGame().setGameLocation(location);
                return;
            case "death_match":
                plugin.getGame().setDeathMatchLocation(location);
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
