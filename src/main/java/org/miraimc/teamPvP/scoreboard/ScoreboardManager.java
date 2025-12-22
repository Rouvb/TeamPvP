package org.miraimc.teamPvP.scoreboard;

import fr.mrmicky.fastboard.adventure.FastBoard;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.game.Game;
import org.miraimc.teamPvP.game.GameSettings;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
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

        String title = plugin.getScoreboardFile().getRawString("scoreboard.title");
        board.updateTitle(ColorParser.of(title).legacy().papi(player).build());

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
                return team == null ? "starting" : "starting_team";
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

    private List<Component> getComponentList(List<String> lines, Player player) {
        List<Component> componentList = new ArrayList<>();
        Team team = plugin.getTeamManager().getTeam(player);
        String teamName = team != null ? team.getName() : "없음";
        String teamDisplayName = team != null ? team.getDisplayName() : "없음";
        Component teamPrefix = team != null ? ColorParser.of(team.getPrefix()).build() : Component.empty();
        Component teamColor = team != null ? ColorParser.of(team.getColor()).build() : Component.empty();
        String teamLeaderName = team != null ? plugin.getProfileHandler().getPlayerName(team.getLeader()) : "없음";
        String teamSize = team != null ? String.valueOf(team.getMembers().size()) : "0";
        String teamAlive = team != null ? String.valueOf(team.getAliveMembers().size()) : "0";

        Game game = plugin.getGame();
        GameSettings gameSettings = plugin.getGameSettings();
        String gameCountdown = String.valueOf(gameSettings.getCountdownTime());
        String gameDuration = TimeFormatUtil.formatSeconds(game.getGameTime());

        for (String line : lines) {
            if (plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                line = PlaceholderAPI.setPlaceholders(player, line);
            }

            componentList.add(
                    ColorParser.of(line)
                            .with("team_name", teamName)
                            .with("team_display_name", teamDisplayName)
                            .with("team_prefix", teamPrefix)
                            .with("team_color", teamColor)
                            .with("team_leader", teamLeaderName)
                            .with("team_size", teamSize)
                            .with("team_alive", teamAlive)
                            .with("game_countdown", gameCountdown)
                            .with("game_duration", gameDuration)
                            .with("border_size", getAbsBorderSize(player))
                            .legacy()
                            .papi(player)
                            .build()
            );
        }
        return componentList;
    }

    private void renderScoreboard(FastBoard board, String type) {
        List<String> lines = plugin.getScoreboardFile().getRawStringList("scoreboard." + type);
        board.updateLines(getComponentList(lines, board.getPlayer()));
    }

    private String getAbsBorderSize(Player player) {
        World world = player.getWorld();
        WorldBorder border = world.getWorldBorder();
        return String.format("%,d", (int) (border.getSize() / 2));
    }
}
