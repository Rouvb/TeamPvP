package org.miraimc.teamPvP.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Optional;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

@CommandAlias("teampvp")
public class TeamPvPCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamPvPCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Subcommand("start|시작")
    @CommandPermission("teampvp.start")
    public void onStart(Player sender) {
        sender.sendMessage(TextUtil.translate("&a게임이 곧 시작됩니다."));
        plugin.getGameManager().startCountdown();
    }

    @Subcommand("reload")
    @CommandPermission("teampvp.admin")
    public void onReload(Player sender) {
        plugin.getConfigFile().reload();
        plugin.getScoreboardFile().reload();
        plugin.getTeamsFile().reload();

        sender.sendMessage(TextUtil.translate("&aPlugin has been reloaded."));
    }

    @Subcommand("debug")
    @CommandPermission("teampvp.admin")
    public void onDebug(Player sender, @Optional OnlinePlayer target) {
        Player targetPlayer;
        if (target == null) {
            targetPlayer = sender;
        } else {
            targetPlayer = target.getPlayer();
        }

        Team team = plugin.getTeamManager().getTeam(targetPlayer);
        Profile profile = plugin.getProfileManager().getProfile(targetPlayer);

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        sender.sendMessage(TextUtil.translate("&b&lLocations:"));
        sender.sendMessage(TextUtil.translate(" &fLobby: &b" + plugin.getConfigFile().getString("game.locations.lobby")));
        sender.sendMessage(TextUtil.translate(" &fGame: &b" + plugin.getConfigFile().getString("game.locations.game")));
        sender.sendMessage(" ");

        if (profile != null) {
            sender.sendMessage(TextUtil.translate("&b&lProfile Info:"));
            sender.sendMessage(TextUtil.translate(" &fStatus: &b" + profile.getStatus().name()));
            sender.sendMessage(TextUtil.translate(" &fBuild Mode: &b" + profile.isBuildMode()));
            sender.sendMessage(" ");
        }

        if (team != null) {
            sender.sendMessage(TextUtil.translate("&b&lTeam Info:"));
            sender.sendMessage(TextUtil.translate(" &fTeam: &b" + team.getName()));
            sender.sendMessage(TextUtil.translate(" &fTeam Display Name: &b" + team.getColor() + team.getDisplayName()));
            sender.sendMessage(TextUtil.translate(" &fTeam Prefix: &b" + team.getPrefix()));
            sender.sendMessage(TextUtil.translate(" &fTeam Leader: &b" + team.getLeader()));
            sender.sendMessage(TextUtil.translate(" &fTeam Members: &b" + plugin.getTeamHandler().parseTeamMembers(team)));
        }

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }
}
