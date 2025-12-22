package org.miraimc.teamPvP.game.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.game.setting.GameSettings;

public class CountdownTask extends BukkitRunnable {

    private final GameManager manager;
    private final GameSettings gameSettings;

    public CountdownTask(GameManager manager, GameSettings gameSettings) {
        this.manager = manager;
        this.gameSettings = gameSettings;
    }

    @Override
    public void run() {
        if (gameSettings.getCountdownTime() == 0) {
            manager.soundAll(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
            manager.startGame();
            cancel();
        } else if (gameSettings.getCountdownTime() % 5 == 0 || gameSettings.getCountdownTime() <= 5) {
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_PLING);
            manager.messageAll(Lang.GAME_STATUS_COUNTDOWN_MESSAGE.getMessage()
                    .replace("{time}", String.valueOf(gameSettings.getCountdownTime())));
            Title title = Title.title(
                    Component.text(Lang.GAME_STATUS_COUNTDOWN_TITLE.getMessage()
                            .replace("{time}", String.valueOf(gameSettings.getCountdownTime()))
                    ),
                    Component.text(Lang.GAME_STATUS_COUNTDOWN_SUBTITLE.getMessage()
                            .replace("{time}", String.valueOf(gameSettings.getCountdownTime()))
                    )
            );
            manager.TitleAll(title);
        }
        gameSettings.setCountdownTime(gameSettings.getCountdownTime() - 1);
    }
}
