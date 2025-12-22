package org.miraimc.teamPvP.game;

import lombok.Getter;
import lombok.Setter;
import org.miraimc.teamPvP.TeamPvP;

@Getter
@Setter
public class GameSettings {

    private final TeamPvP plugin;
    private int countdownTime;
    private int maxGameTime;
    private int deathMatchTime;
    private int invincibilityTime;
    private int defaultBorderSize;
    private int deathMatchBorderSize;

    public GameSettings(TeamPvP plugin) {
        this.plugin = plugin;
        this.countdownTime = plugin.getConfigFile().getInt("game.countdown_time");
        this.maxGameTime = plugin.getConfigFile().getInt("game.max_game_time");
        this.deathMatchTime = plugin.getConfigFile().getInt("game.death_match_time");
        this.invincibilityTime = plugin.getConfigFile().getInt("game.invincibility_time");
        this.defaultBorderSize = plugin.getConfigFile().getInt("game.world_border.default");
        this.deathMatchBorderSize = plugin.getConfigFile().getInt("game.world_border.death_match");
    }
}
