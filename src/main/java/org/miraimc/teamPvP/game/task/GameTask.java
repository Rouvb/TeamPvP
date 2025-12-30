package org.miraimc.teamPvP.game.task;

import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.setting.GameSettings;
import org.miraimc.teamPvP.team.Team;

import java.util.List;
import java.util.Set;

public class GameTask extends BukkitRunnable {

    private final TeamPvP plugin;

    private static final Set<Integer> ALERT_TIMES = Set.of(
            1800,
            1200,
            900,
            600,
            300,
            240,
            180,
            120,
            60,
            30,
            15,
            10,
            5,
            4,
            3,
            2,
            1
    );

    public GameTask(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Game game = plugin.getGame();
        GameSettings gameSettings = plugin.getGameSettings();
        GameManager manager = plugin.getGameManager();

        game.setGameTime(game.getGameTime() + 1);

        int remainingInvincibility = gameSettings.getInvincibilityTime() - game.getGameTime();
        int remainingDeathMatch = gameSettings.getDeathMatchTime() - game.getGameTime();

        if (ALERT_TIMES.contains(remainingInvincibility)) {
            manager.messageAll(Lang.GAME_INVINCIBILITY.getMessage()
                    .replace("{time}", formatTime(remainingInvincibility)));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        if (ALERT_TIMES.contains(remainingDeathMatch)) {
            manager.messageAll(Lang.GAME_STATUS_DEATHMATCH_ALERT.getMessage()
                    .replace("{time}", formatTime(remainingDeathMatch)));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        if (plugin.getConfigFile().getBoolean("game.action_bar.leader_location.enabled")) {
            sendLeaderLocationActionBar();
        }

        if (remainingDeathMatch <= 0) {
            manager.startDeathMatch();
        }

        if (gameSettings.getMaxGameTime() <= game.getGameTime()) {
            manager.endGame(null);
            cancel();
        }
    }

    private void sendLeaderLocationActionBar() {
        List<Team> teams = plugin.getTeamManager().getTeams();
        for (Team team : teams) {
            Player leader = Bukkit.getPlayer(team.getLeader());
            if (leader == null || !leader.isOnline()) continue;
            plugin.getTeamManager().sendActionBar(team,
                    ColorParser.of(plugin.getConfigFile().getString("game.action_bar.leader_location.message"))
                            .with("team_display_name", team.getDisplayName())
                            .with("team_prefix", team.getPrefix())
                            .with("team_leader", leader.getName())
                            .with("team_leader_coords", getFormattedCoords(leader.getLocation()))
                            .papi(leader)
                            .legacy()
                            .build()
            );
        }
    }

    private String getFormattedCoords(Location location) {
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        return (x + "," + y + "," + z);
    }

    private String formatTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            return minutes + Lang.UNIT_MINUTE.getMessage();
        } else {
            return seconds + Lang.UNIT_SECOND.getMessage();
        }
    }
}
