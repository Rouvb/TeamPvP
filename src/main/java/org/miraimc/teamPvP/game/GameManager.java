package org.miraimc.teamPvP.game;

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

public class GameManager {

    private final TeamPvP plugin;

    private BukkitTask countdownTask;
    private BukkitTask gameTask;

    public GameManager(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void startCountdown() {
        if (plugin.getGame().getGameStatus() != org.miraimc.teamPvP.game.GameStatus.LOBBY) return;

        plugin.getGame().setGameStatus(org.miraimc.teamPvP.game.GameStatus.STARTING);

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
        if (plugin.getGame().getGameStatus() != org.miraimc.teamPvP.game.GameStatus.STARTING) return;

        plugin.getGame().setGameStatus(org.miraimc.teamPvP.game.GameStatus.PLAYING);

        cancelCountdown();

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getOnlinePlayers().forEach(player -> {
                plugin.getGameHandler().preparePlayerForGame(player);
                plugin.getProfileManager().getProfile(player).setStatus(ProfileStatus.PLAYING);
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
        if (plugin.getGame().getGameStatus() != org.miraimc.teamPvP.game.GameStatus.PLAYING) return;
        plugin.getGame().setGameStatus(org.miraimc.teamPvP.game.GameStatus.DEATH_MATCH);
        plugin.getGameHandler().sendDeathMatchAlert();
        plugin.getGameHandler().teleportAllToDeathMatch();
    }

    public void endGame() {
        org.miraimc.teamPvP.game.GameStatus gameStatus = plugin.getGame().getGameStatus();
        if (gameStatus != org.miraimc.teamPvP.game.GameStatus.PLAYING && gameStatus != org.miraimc.teamPvP.game.GameStatus.DEATH_MATCH) return;
        plugin.getGame().setGameStatus(org.miraimc.teamPvP.game.GameStatus.ENDED);
    }

    public void setSpectatorMode(Player player) {
        Team team = plugin.getTeamManager().getTeam(player);
        team.getMembers().remove(player.getUniqueId());
        Profile profile = plugin.getProfileManager().getProfile(player);
        profile.setStatus(ProfileStatus.SPECTATING);
        plugin.getGameHandler().setSpectatorMode(player);
    }

    public void loadLocation(String locationType) {
        Location location = LocationUtil.deserializeLocation(plugin.getConfigFile().getString("game.locations." + locationType));
        if (location == null) return;
        switch (locationType) {
            case "lobby":
                plugin.getGame().setGameStatus(org.miraimc.teamPvP.game.GameStatus.LOBBY);
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
