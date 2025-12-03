package org.miraimc.teamPvP.game.task;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.util.TextUtil;

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

        int remainingInvincibility = game.getInvincibilityTime() - game.getDuration();
        int remainingDeathMatch = game.getDeathMatchTime() - game.getDuration();

        if (ALERT_TIMES.contains(remainingInvincibility)) {
            manager.messageAll(TextUtil.translate("&f무적 시간이 &b" + formatTime(remainingInvincibility) + "&f 후에 해제됩니다!"));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        if (ALERT_TIMES.contains(remainingDeathMatch)) {
            manager.messageAll(TextUtil.translate("&f데스 매치가 &b" + formatTime(remainingDeathMatch) + "&f 후에 시작됩니다!"));
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_BELL);
        }

        if (remainingDeathMatch <= 0) {
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
