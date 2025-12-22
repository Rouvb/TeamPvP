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
    private int invincibilityTime;
    private int deathMatchTime;
    private boolean deathMatchArenaEnabled;
    private int defaultBorderSize;
    private int deathMatchBorderSize;
    private int rtpRange;

    public GameSettings(TeamPvP plugin) {
        this.plugin = plugin;
        this.countdownTime = plugin.getConfigFile().getInt("game.countdown_time");
        this.maxGameTime = plugin.getConfigFile().getInt("game.max_game_time");
        this.invincibilityTime = plugin.getConfigFile().getInt("game.invincibility_time");
        this.deathMatchTime = plugin.getConfigFile().getInt("death_match.start_time");
        this.deathMatchArenaEnabled = plugin.getConfigFile().getBoolean("death_match.arena_enabled");
        this.defaultBorderSize = plugin.getConfigFile().getInt("game.border");
        this.deathMatchBorderSize = plugin.getConfigFile().getInt("death_match.border");
        this.rtpRange = plugin.getConfigFile().getInt("rtp.range");
    }
}
