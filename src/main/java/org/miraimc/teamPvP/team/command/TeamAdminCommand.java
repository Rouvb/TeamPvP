package org.miraimc.teamPvP.team.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.util.LocationUtil;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("team")
@CommandPermission("teampvp.admin")
public class TeamAdminCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamAdminCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Subcommand("setdeathmatch")
    @CommandPermission("teampvp.admin")
    public void onDeathMatchAdd(Player sender, String teamName) {
        Location location = sender.getLocation();
        plugin.getTeamManager().getTeam(teamName).setDeathMatchLocation(location);
        plugin.getTeamsFile().set("teams." + teamName + ".death_match_location", LocationUtil.serializeLocation(location));
        sender.sendMessage(TextUtil.translate("&aSuccessfully added death match location for &e" + teamName + "&a."));
    }
}
