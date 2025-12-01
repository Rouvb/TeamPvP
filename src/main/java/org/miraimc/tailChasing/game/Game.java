package org.miraimc.tailChasing.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.miraimc.tailChasing.TailChasing;

@Getter
@Setter
public class Game {

    private final TailChasing plugin;
    private GameStatus gameStatus;
    private int duration;
    private int countdownTime;
    private int deathMatchTime;
    private int borderSize;
    private Location lobbyLocation;
    private Location gameLocation;

    public Game(TailChasing plugin, GameStatus gameStatus) {
        this.plugin = plugin;
        this.gameStatus = gameStatus;
        this.duration = 0;
        this.countdownTime = plugin.getConfigFile().getInt("game.countdown_time");
        this.deathMatchTime = plugin.getConfigFile().getInt("game.death_match_time");
        this.borderSize = plugin.getConfigFile().getInt("game.border_size");
    }
}
