package org.miraimc.teamPvP.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
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

        Team team = plugin.getTeamManager().getTeam(player);

        return switch (params.toLowerCase()) {
            case "player_status" -> profile.getStatus().toString();
            case "team_weight"   -> team != null ? String.valueOf(team.getWeight()) : "0";
            case "team_name"     -> team != null ? team.getName() : "";
            case "team_color"    -> team != null ? team.getColor() : "";
            case "team_prefix"   -> team != null ? team.getPrefix() : "";
            case "team_size"     -> team != null ? String.valueOf(team.getMembers().size()) : "0";
            case "team_alive"    -> team != null ? String.valueOf(team.getAliveMembers().size()) : "0";
            case "player_color"  -> {
                if (profile.getStatus().equals(ProfileStatus.SPECTATING)) {
                    yield TextUtil.translate("&7");
                } else if (team != null) {
                    yield team.getColor();
                } else {
                    yield "";
                }
            }
            default -> "";
        };
    }
}
