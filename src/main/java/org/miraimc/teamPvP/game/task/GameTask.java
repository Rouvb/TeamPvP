package org.miraimc.teamPvP.game.task;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.setting.GameSettings;
import org.miraimc.teamPvP.team.Team;

import java.util.Set;
import java.util.UUID;

public class GameTask extends BukkitRunnable {

    private final TeamPvP plugin;

    private static final Set<Integer> ALERT_TIMES = Set.of(
            1800, 1200, 900, 600, 300, 240, 180, 120,
            60, 30, 15, 10, 5, 4, 3, 2, 1
    );

    public GameTask(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Game game = plugin.getGame();
        GameSettings settings = plugin.getGameSettings();
        GameManager manager = plugin.getGameManager();

        int currentTime = game.getGameTime() + 1;
        game.setGameTime(currentTime);

        int invincibilityRemaining = settings.getInvincibilityTime() - currentTime;
        int deathMatchRemaining = settings.getDeathMatchTime() - currentTime;

        checkAndAlert(invincibilityRemaining, Lang.GAME_INVINCIBILITY.getMessage(), manager);
        checkAndAlert(deathMatchRemaining, Lang.GAME_STATUS_DEATHMATCH_ALERT.getMessage(), manager);

        if (plugin.getConfigFile().getBoolean("game.action_bar.leader_location.enabled")) {
            sendLeaderLocationActionBar();
        }

        if (deathMatchRemaining <= 0) {
            manager.startDeathMatch();
        }

        if (settings.getMaxGameTime() <= currentTime) {
            manager.endGame(null);
            cancel();
        }
    }

    private void checkAndAlert(int remainingTime, String messageTemplate, GameManager manager) {
        if (ALERT_TIMES.contains(remainingTime)) {
            manager.messageAll(messageTemplate.replace("{time}", formatTime(remainingTime)));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }
    }

    private void sendLeaderLocationActionBar() {
        String actionBarMessage = plugin.getConfigFile().getString("game.action_bar.leader_location.message");

        for (Team team : plugin.getTeamManager().getTeams()) {
            UUID leaderUUID = team.getLeader();
            if (leaderUUID == null) continue;

            Player leader = Bukkit.getPlayer(leaderUUID);
            if (leader == null || !leader.isOnline()) continue;

            plugin.getTeamManager().sendActionBar(
                    team,
                    ColorParser.of(actionBarMessage)
                            .papi(leader)
                            .legacy()
                            .build()
            );
        }
    }

    private String formatTime(int seconds) {
        return (seconds >= 60)
                ? (seconds / 60) + Lang.UNIT_MINUTE.getMessage()
                : seconds + Lang.UNIT_SECOND.getMessage();
    }
}