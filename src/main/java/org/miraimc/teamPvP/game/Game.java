package org.miraimc.teamPvP.game;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;
import org.miraimc.teamPvP.TeamPvP;

@Getter
@Setter
public class Game {

    private final TeamPvP plugin;
    private org.miraimc.teamPvP.game.GameStatus gameStatus;
    private int duration;
    private int countdownTime;
    private int deathMatchTime;
    private int invincibilityTime;
    private int borderSize;
    private Location lobbyLocation;
    private Location gameLocation;

    public Game(TeamPvP plugin, org.miraimc.teamPvP.game.GameStatus gameStatus) {
        this.plugin = plugin;
        this.gameStatus = gameStatus;
        this.duration = 0;
        this.countdownTime = plugin.getConfigFile().getInt("game.countdown_time");
        this.invincibilityTime = plugin.getConfigFile().getInt("game.invincibility_time");
        this.borderSize = plugin.getConfigFile().getInt("game.world_border.default");
    }
}
