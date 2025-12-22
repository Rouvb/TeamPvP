package org.miraimc.teamPvP.game.task;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.GameSettings;

import java.util.Set;

public class GameTask extends BukkitRunnable {

    private final Game game;
    private final GameSettings gameSettings;
    private final GameManager manager;

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

    public GameTask(Game game, GameSettings gameSettings, GameManager manager) {
        this.game = game;
        this.gameSettings = gameSettings;
        this.manager = manager;
    }

    @Override
    public void run() {
        game.setDuration(game.getDuration() + 1);

        int remainingInvincibility = gameSettings.getInvincibilityTime() - game.getDuration();
        int remainingDeathMatch = gameSettings.getDeathMatchTime() - game.getDuration();

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

        if (remainingDeathMatch <= 0) {
            manager.startDeathMatch();
        }
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
