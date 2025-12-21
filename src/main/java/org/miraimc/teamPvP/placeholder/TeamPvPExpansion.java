package org.miraimc.teamPvP.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.profile.ProfileStatus;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

public class TeamPvPExpansion extends PlaceholderExpansion {

    private final TeamPvP plugin;

    public TeamPvPExpansion(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "teampvp";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MiraiMC Development";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        Profile profile = plugin.getProfileManager().getProfile(player);
        if (profile == null) return "";

        Team playerTeam = plugin.getTeamManager().getTeam(player);

        if (params.toLowerCase().startsWith("team_size_")) {
            String teamName = params.toLowerCase().replace("team_size_", "");
            return getTeamSize(teamName);
        }

        return switch (params.toLowerCase()) {
            case "player_status" -> profile.getStatus().toString();
            case "team_weight" -> playerTeam != null ? String.valueOf(playerTeam.getWeight()) : "0";
            case "team_name" -> playerTeam != null ? playerTeam.getName() : "";
            case "team_display_name" -> playerTeam != null ? playerTeam.getDisplayName() : "";
            case "team_color" -> playerTeam != null ? playerTeam.getColor() : "";
            case "team_prefix" -> playerTeam != null ? playerTeam.getPrefix() : "";
            case "team_size" -> playerTeam != null ? String.valueOf(playerTeam.getMembers().size()) : "0";
            case "team_alive" -> playerTeam != null ? String.valueOf(playerTeam.getAliveMembers().size()) : "0";
            case "team_leader" -> playerTeam != null ? Bukkit.getOfflinePlayer(playerTeam.getLeader()).getName() : "";
            case "player_color" -> {
                if (profile.getStatus().equals(ProfileStatus.SPECTATING)) {
                    yield TextUtil.translate("&7");
                } else if (playerTeam != null) {
                    yield playerTeam.getColor();
                } else {
                    yield "";
                }
            }
            default -> "";
        };
    }

    private String getTeamSize(String teamName) {
        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) return "";
        return String.valueOf(team.getAliveMembers().size());
    }
}
