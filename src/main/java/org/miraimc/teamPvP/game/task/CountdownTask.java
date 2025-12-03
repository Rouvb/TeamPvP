package org.miraimc.teamPvP.game.task;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameManager;
import org.miraimc.teamPvP.util.TextUtil;

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
            manager.messageAll(TextUtil.translate("&f게임이 &b{time}&f초 후 시작됩니다..."
                    .replace("{time}", String.valueOf(game.getCountdownTime()))
            ));
            Title title = Title.title(
                    Component.text(TextUtil.translate("&b&lTeam PvP")),
                    Component.text(TextUtil.translate("&f게임이 &b{time}&f초 후에 시작됩니다.")
                            .replace("{time}", String.valueOf(game.getCountdownTime()))
                    )
            );
            manager.TitleAll(title);
        }
        game.setCountdownTime(game.getCountdownTime() - 1);
    }
}
