package org.miraimc.teamPvP.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.miraimc.teamPvP.TeamPvP;

@Getter
@Setter
public class Game {

    private final TeamPvP plugin;
    private GameStatus gameStatus;
    private int duration;
    private Location lobbyLocation;
    private Location gameLocation;

    public Game(TeamPvP plugin, GameStatus gameStatus) {
        this.plugin = plugin;
        this.gameStatus = gameStatus;
        this.duration = 0;
    }
}
