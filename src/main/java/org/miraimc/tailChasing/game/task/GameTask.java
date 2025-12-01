package org.miraimc.tailChasing.game.task;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.tailChasing.game.Game;
import org.miraimc.tailChasing.game.GameManager;
import org.miraimc.tailChasing.util.TextUtil;

import java.util.Set;

public class GameTask extends BukkitRunnable {

    private final Game game;
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

    public GameTask(Game game, GameManager manager) {
        this.game = game;
        this.manager = manager;
    }

    @Override
    public void run() {
        game.setDuration(game.getDuration() + 1);

        int remaining = game.getDeathMatchTime() - game.getDuration();

        if (ALERT_TIMES.contains(remaining)) {
            manager.messageAll(TextUtil.translate("&f데스 매치가 &b" + formatTime(remaining) + "&f 후에 시작됩니다!"));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        if (remaining <= 0) {
            manager.startDeathMatch();
        }
    }

    private String formatTime(int seconds) {
        if (seconds >= 60) {
            int minutes = seconds / 60;
            return minutes + TextUtil.translate("&f분");
        } else {
            return seconds + TextUtil.translate("&f초");
        }
    }
}
