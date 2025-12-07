package org.miraimc.teamPvP.team.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Subcommand;
import co.aikar.commands.bukkit.contexts.OnlinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.miraimc.teamPvP.TeamPvP;
import org.miraimc.teamPvP.config.Lang;
import org.miraimc.teamPvP.game.GameStatus;
import org.miraimc.teamPvP.profile.ChatChannel;
import org.miraimc.teamPvP.profile.Profile;
import org.miraimc.teamPvP.team.Team;
import org.miraimc.teamPvP.util.TextUtil;

import java.util.List;

@CommandAlias("team|팀")
public class TeamCommand extends BaseCommand {

    private final TeamPvP plugin;

    public TeamCommand(TeamPvP plugin) {
        this.plugin = plugin;
    }

    @Default
    public void onCommand(CommandSender sender) {
        List<String> messages = Lang.TEAM_COMMAND_HELP.getMessageList();
        messages.forEach(sender::sendMessage);
    }

    @Subcommand("list|목록")
    @CommandPermission("teampvp.team.list")
    public void onList(Player sender) {
        List<Team> teams = plugin.getTeamManager().getTeams();

        sender.sendMessage(TextUtil.LONG_LINE);
        sender.sendMessage(TextUtil.translate("&b&l팀 목록:"));
        for (Team team : teams) {
            String message = String.format(" %s%s &7(%d): %s",
                    team.getPrefix(),
                    team.getDisplayName(),
                    team.getAliveMembers().size(),
                    plugin.getTeamHandler().parseTeamMembers(team));
            sender.sendMessage(TextUtil.translate(message));
        }
        sender.sendMessage(TextUtil.LONG_LINE);
    }

    @Subcommand("join|참가")
    public void onJoin(Player sender) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(Lang.TEAM_ERROR_UNABLE_ACCESS.getMessage());
            return;
        }

        Team team = plugin.getTeamManager().getRandomTeam();
        if (team == null) {
            sender.sendMessage(Lang.TEAM_ERROR_NOT_FOUND.getMessage());
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("forcejoin|강제참가")
    @CommandPermission("teampvp.team.forcejoin")
    public void onForceJoin(Player sender, String teamName) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(Lang.TEAM_ERROR_UNABLE_ACCESS.getMessage());
            return;
        }

        Team team = plugin.getTeamManager().getTeam(teamName);
        if (team == null) {
            sender.sendMessage(Lang.TEAM_ERROR_NOT_FOUND.getMessage());
            return;
        }

        plugin.getTeamHandler().handleTeam(team, sender);
    }

    @Subcommand("leave|탈퇴")
    public void onLeave(Player sender) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(Lang.TEAM_ERROR_UNABLE_ACCESS.getMessage());
            return;
        }

        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(Lang.TEAM_ERROR_NO_TEAM.getMessage());
            return;
        }

        team.getMembers().remove(sender.getUniqueId());
    }

    @Subcommand("info|정보")
    public void onInfo(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null) {
            sender.sendMessage(Lang.TEAM_ERROR_NO_TEAM.getMessage());
            return;
        }

        String leaderName = plugin.getProfileHandler().getPlayerName(team.getLeader());

        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&b&l팀 정보:")));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 팀: " + team.getPrefix() + " " + team.getDisplayName())));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 리더: " + team.getColor() + leaderName)));
        sender.sendMessage(TextUtil.translate(TextUtil.translate("&f 멤버 (" + team.getMembers().size() + "): " + plugin.getTeamHandler().parseTeamMembers(team))));
        sender.sendMessage(TextUtil.translate(TextUtil.LONG_LINE));
    }

    @Subcommand("leader|리더")
    public void onLeader(Player sender, OnlinePlayer target) {
        if (plugin.getGame().getGameStatus() != GameStatus.LOBBY) {
            sender.sendMessage(Lang.TEAM_ERROR_UNABLE_ACCESS.getMessage());
            return;
        }

        Team team = plugin.getTeamManager().getTeam(sender);
        if (team == null || !team.getMembers().contains(target.getPlayer().getUniqueId())) {
            sender.sendMessage(Lang.TEAM_ERROR_NO_TEAM.getMessage());
            return;
        }

        if (!team.getLeader().equals(sender.getUniqueId())) {
            sender.sendMessage(Lang.TEAM_ERROR_ONLY_LEADER.getMessage());
        } else {
            team.setLeader(target.getPlayer().getUniqueId());
            sender.sendMessage(Lang.TEAM_STATUS_NEW_LEADER.getMessage()
                    .replace("{team_color}", team.getColor())
                    .replace("{player}", target.getPlayer().getName())
            );
        }
    }

    @Subcommand("chat|채팅")
    public void onChat(Player sender) {
        Team team = plugin.getTeamManager().getTeam(sender);
        Profile profile = plugin.getProfileManager().getProfile(sender);
        if (team == null) {
            profile.setChatChannel(ChatChannel.GLOBAL);
            sender.sendMessage(Lang.TEAM_ERROR_NO_TEAM.getMessage());
            return;
        }

        if (profile.getChatChannel() == ChatChannel.GLOBAL) {
            profile.setChatChannel(ChatChannel.TEAM);
            sender.sendMessage(Lang.TEAM_CHAT_TOGGLE_CHANNEL.getMessage()
                    .replace("{channel}", Lang.TEAM_CHAT_CHANNEL_TEAM.getMessage()));
            return;
        }
        if (profile.getChatChannel() == ChatChannel.TEAM) {
            profile.setChatChannel(ChatChannel.GLOBAL);
            sender.sendMessage(Lang.TEAM_CHAT_TOGGLE_CHANNEL.getMessage()
                    .replace("{channel}", Lang.TEAM_CHAT_CHANNEL_GLOBAL.getMessage()));
        }
    }
}
