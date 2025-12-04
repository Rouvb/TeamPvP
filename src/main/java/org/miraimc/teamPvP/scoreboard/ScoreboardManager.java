package org.miraimc.teamPvP.scoreboard;

import fr.mrmicky.fastboard.FastBoard;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.PlayerUtil;
import org.miraimc.teamPvP.util.TextUtil;
import org.miraimc.teamPvP.util.TimeFormatUtil;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardManager {

    private final TeamPvP plugin;

    public ScoreboardManager(TeamPvP plugin) {
        this.plugin = plugin;
    }

    public void updateScoreboard(FastBoard board) {
        Player player = board.getPlayer();
        if (player == null || !player.isOnline() || board.isDeleted()) return;

        board.updateTitle(plugin.getScoreboardFile().getString("scoreboard.title"));

        Game game = plugin.getGame();
        Team team = plugin.getTeamManager().getTeam(player);
        Profile profile = plugin.getProfileManager().getProfile(player);

        String type = resolveScoreboardType(game.getGameStatus(), team, profile);
        renderScoreboard(board, type);
    }

    private String resolveScoreboardType(GameStatus status, Team team, Profile profile) {
        switch (status) {
            case LOBBY:
                return team == null ? "lobby" : "lobby_team";
            case STARTING:
                return "starting";
            case PLAYING:
            case DEATH_MATCH:
                if (profile.getStatus() == ProfileStatus.SPECTATING) {
                    return team != null ? "spectating_team" : "spectating";
                }
                return "playing";
            case ENDING:
                return "ending";
            default:
                return "default";
        }
    }

    private List<String> getFormattedLines(List<String> lines, Player player) {
        List<String> formattedLines = new ArrayList<>();
        Team team = plugin.getTeamManager().getTeam(player);
        String teamName = team != null ? team.getName() : "없음";
        String teamDisplayName = team != null ? team.getDisplayName() : "없음";
        String teamPrefix = team != null ? team.getPrefix() : "";
        String teamColor = team != null ? team.getColor() : "";
        String teamLeaderName = team != null ? PlayerUtil.getPlayerName(team.getLeader()) : "없음";
        String teamSize = team != null ? String.valueOf(team.getMembers().size()) : "0";
        String teamAlive = team != null ? String.valueOf(team.getAliveMembers().size()) : "0";

        Game game = plugin.getGame();
        String gameCountdown = String.valueOf(game.getCountdownTime());
        String gameDuration = TimeFormatUtil.formatSeconds(game.getDuration());

        for (String line : lines) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            formattedLines.add(TextUtil.translate(line)
                    .replace("{team_name}", teamName)
                    .replace("{team_display_name}", teamDisplayName)
                    .replace("{team_prefix}", teamPrefix)
                    .replace("{team_color}", teamColor)
                    .replace("{team_leader}", teamLeaderName)
                    .replace("{team_size}", teamSize)
                    .replace("{team_alive}", teamAlive)
                    .replace("{game_countdown}", gameCountdown)
                    .replace("{game_duration}", gameDuration)
            );
        }
        return formattedLines;
    }

    private void renderScoreboard(FastBoard board, String type) {
        List<String> lines = plugin.getScoreboardFile().getStringList("scoreboard." + type);
        board.updateLines(getFormattedLines(lines, board.getPlayer()));
    }
}
