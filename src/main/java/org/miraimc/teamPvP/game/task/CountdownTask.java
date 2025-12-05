package org.miraimc.teamPvP.game.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;

public class CountdownTask extends BukkitRunnable {

    private final GameManager manager;
    private final Game game;

    public CountdownTask(GameManager manager, Game game) {
        this.manager = manager;
        this.game = game;
    }

    @Override
    public void run() {
        if (game.getCountdownTime() == 0) {
            manager.soundAll(Sound.ENTITY_FIREWORK_ROCKET_BLAST);
            manager.startGame();
            cancel();
        } else if (game.getCountdownTime() % 5 == 0 || game.getCountdownTime() <= 5) {
            manager.soundAll(Sound.BLOCK_NOTE_BLOCK_PLING);
            manager.messageAll(Lang.GAME_STATUS_COUNTDOWN_MESSAGE.getMessage()
                    .replace("{time}", String.valueOf(game.getCountdownTime())));
            Title title = Title.title(
                    Component.text(Lang.GAME_STATUS_COUNTDOWN_TITLE.getMessage()
                            .replace("{time}", String.valueOf(game.getCountdownTime()))
                    ),
                    Component.text(Lang.GAME_STATUS_COUNTDOWN_SUBTITLE.getMessage()
                            .replace("{time}", String.valueOf(game.getCountdownTime()))
                    )
            );
            manager.TitleAll(title);
        }
        game.setCountdownTime(game.getCountdownTime() - 1);
    }
}
