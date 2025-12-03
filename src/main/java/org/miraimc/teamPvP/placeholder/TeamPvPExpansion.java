package org.miraimc.teamPvP.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
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
        if (params.equalsIgnoreCase("player_status")) return profile.getStatus().toString();

        Team team = plugin.getTeamManager().getTeam(player);
        if (team == null) return "";

        return switch (params) {
            case "team_name" -> team.getName();
            case "team_color" -> team.getColor();
            case "team_prefix" -> team.getPrefix();
            case "team_size" -> String.valueOf(team.getMembers().size());
            case "team_alive" -> String.valueOf(team.getAliveMembers().size());
            case "player_color" -> team.getAliveMembers().contains(player.getUniqueId()) ? team.getColor() : TextUtil.translate("&7");
            default -> "";
        };
    }
}
